var net = require('net'),
    sys = require('sys'),
    buf = require('buffer');


/*
    Maintains "virtual" buffer as a collection of Node buffers.

    Used to treat data fragmentation on reads and kernel buffers overflows
    on writes with no need to relocate existing data.

*/
var CompositeBuffer = exports.CompositeBuffer = function() {
    this.length = 0;
    this.readerIndex = 0;
    this.buffers = [];
};

/*
    Write to virtual buffer.

 */
CompositeBuffer.prototype.write = function(buffer) {
    if(buffer == undefined || buffer == null || buffer.length == 0) {
        return;
    }

    this.buffers.push(buffer);
    this.length += buffer.length;
}

/*
    Read from the virtual buffer.

    Reader index is not changed.

 */
CompositeBuffer.prototype.readNoAdvance = function(size) {
    if(size > this.length) {
        throw "Error: Index out of bounds";
    }

    var buffer = new buf.Buffer(size);

    if(size == 0) {
        return buffer;
    }

    var copied = 0;
    var tempReaderIndex = this.readerIndex;
    var bufferIndex = 0;

    do {
        var currentBuffer = this.buffers[bufferIndex];
        var toRead = ((currentBuffer.length - tempReaderIndex) < (size - copied))
                ? (currentBuffer.length - tempReaderIndex) : (size - copied);
        currentBuffer.copy(buffer, copied, tempReaderIndex, tempReaderIndex + toRead);

        copied += toRead;
        tempReaderIndex += toRead;

        if (tempReaderIndex == currentBuffer.length) {
            tempReaderIndex = 0;
            bufferIndex++;
        }

    } while (copied != size);

    return buffer;

}

/*
    Rewind the virtual buffer.

    Reader index is changed and skipped buffers are dropped.

 */
CompositeBuffer.prototype.advance = function(size) {
    if(size > this.length) {
        throw "Error: Index out of bounds";
    }

    if(size == 0) {
        return;
    }

    var advanced = 0;

    do {
        var toAdvance = ((this.buffers[0].length - this.readerIndex) < (size - advanced))
                ? (this.buffers[0].length - this.readerIndex) : (size - advanced);

        advanced += toAdvance;
        this.readerIndex += toAdvance;

        if(this.readerIndex == this.buffers[0].length) {
            this.readerIndex = 0;
            this.buffers.shift();
        }

    } while(advanced != size);

    this.length -= size;
}

/*
    Read from the virtual buffer.

    Reader index is changed and wasted buffers are dropped.

 */
CompositeBuffer.prototype.read = function(size) {
    var buffer = this.readNoAdvance(size);
    this.advance(size);
    return buffer;
}

/*
    JBNode client constructor function

    Parameters:
        port - port to bind to
        host - hostname or IP to bind to
 */
var JBNode = exports.JBNode = function(port, host) {
    this.port = port;
    this.host = host;

    this.connection = null;
    this.callbacks = [];

    this.receivedDataBuffer = new CompositeBuffer();
    this.writeBuffer = new CompositeBuffer();
    this.writeStopped = false;
};

/*
    JBNode client shall emit "connect" and "disconnect" signals.

 */
sys.inherits(JBNode, process.EventEmitter);

/*
    Setup JBNode client and connect to server.
 */
JBNode.prototype.connect = function () {
	if (!this.connection) {
	    this.connection = new net.createConnection(this.port, this.host);
		var self = this;
	    this.connection.addListener("connect", function () {
	        this.setTimeout(0);
	        this.setNoDelay();
		  	self.emit("connect");
	    });

	    this.connection.addListener("data", function (data) {
            // push new data to composite buffer
	    	self.receivedDataBuffer.write(data);

            self.processBuffer();
	    });

        this.connection.addListener("drain", function () {
            // push new data to composite buffer
            self.drainWriteBuffer();
        });

	    this.connection.addListener("end", function () {
	    	if (self.connection && self.connection.readyState) {
	    		self.connection.end();
	        	self.connection = null;
                this.emit("disconnect");
	      	}
	    });

	    this.connection.addListener("close", function () {
	    	self.connection = null;
	    });
    }
};


/*
    Legitimately close connection to the server.

 */
JBNode.prototype.close = function() {
	if (this.connection && this.connection.readyState === "open") {
		this.connection.end();
	    this.connection = null;

        this.emit("disconnect");
	}
};

/*
    Call Java service configured on JBNode server.

    service     - string in "service.method" format
    data        - data to send
    callback    - function to call on result.

        The result object contains the following variables:
                error (true | false) - error indicator (exists always)
                message - error message thrown by Java service or transport layer (optional)
                data - data (Buffer object) returned by Java service (optional).

*/

JBNode.prototype.call = function(service, data, callback) {
    var serviceLength = service.length;
    var indexOfDot = (serviceLength == 0) ? -1 : service.indexOf('.');
    var failure = null;

    if(serviceLength == 0 || indexOfDot == -1 || service.lastIndexOf('.') != indexOfDot ||
            indexOfDot == 0 || indexOfDot == serviceLength - 1 || service.indexOf('#') != -1) {
        failure = "Service name is malformed."
    }
    if(this.connection == null) {
        failure = "Not connected."
    }

    if(failure == null) {
        this.callbacks.push(callback);
        this.checkedWrite(this.numberToBuffer(buf.Buffer.byteLength(service)));
        this.checkedWrite(new buf.Buffer(service));
        if(data != null) {
            // TODO: utf8 
            this.checkedWrite(this.numberToBuffer(buf.Buffer.byteLength(data, 'utf8')));
            // TODO: utf8 
            this.checkedWrite(new buf.Buffer(data, 'utf8'));
        } else {
            this.checkedWrite(this.numberToBuffer(0));
        }

        return true;
    } else {
        if(callback != null) {
            callback({error: true, message: failure});
        }

        return false;
    }
}

/*
    Used internally to write to open connection.

    Checks are made to protect kernel buffers from overflow. Odd data is preserved by JBNode client.

 */
JBNode.prototype.checkedWrite = function(data) {
    if(!this.writeStopped) {
        if(this.connection.write(data) == false) {
            this.writeStopped = true;
        }
    } else {
        this.writeBuffer.write(data);
    }
}

/*
    Used internally to push preserved data to connection on "drain" ("ready to accept new data") event.

 */
JBNode.prototype.drainWriteBuffer = function() {
    this.writeStopped = false;

    while(this.writeBuffer.length > 0 && !this.writeStopped) {
        var len = (this.writeBuffer.length > 1024) ? 1024 : this.writeBuffer.length;
        var buf = this.writeBuffer.read(len);
        this.checkedWrite(buf);
    }
}


/*
    Encode 4-byte integer to buffer

*/
JBNode.prototype.numberToBuffer = function(n) {
    var x = n;
    var bytes = new buf.Buffer(4);
    var i = 4;

    do {
        bytes[--i] = x & (255);
        x = x >> 8;
    } while (i)

    return bytes;
}

/*
    Decode 4-byte integer from buffer

*/
JBNode.prototype.bufferToNumber = function(b) {
    var x = 0;

    x += (b[0] & (0x0ff)); x = x << 8;
    x += (b[1] & (0x0ff)); x = x << 8;
    x += (b[2] & (0x0ff)); x = x << 8;
    x += (b[3] & (0x0ff));

    return x;
}

/*
    Used internally to check if we have a full response in the buffer.

*/
JBNode.prototype.isFullResponse = function() {
    // we need a minimum message (5 bytes - 4 for length and 1 for marker) to proceed
    if(this.receivedDataBuffer.length < 5) {
        return false;
    }

    var lengthBuffer = this.receivedDataBuffer.readNoAdvance(4);
    var length = this.bufferToNumber(lengthBuffer);

    return this.receivedDataBuffer.length >= 5 + length;
}

/*
    Used internally to read response from the buffer.

*/
JBNode.prototype.readResponse = function() {
    // get the callback for this response
    var callback = this.callbacks.shift();

    // read 5 bytes - 4 for length and 1 for marker
    var lengthBuffer = this.receivedDataBuffer.read(4);
    var marker = this.receivedDataBuffer.read(1)[0];
    var length = this.bufferToNumber(lengthBuffer);

    var payload = this.receivedDataBuffer.read(length);


    if(marker == 46) { // '.' means normal
        response = {error: false, data: payload};
    } else {
        if(marker == 33) { // '!' means exception
            response = {error: true, message: payload};
        } else { // unknown marker, returning error and closing the connection
            response = {error: true, message: "Protocol error: invalid marker(n=" +
                    marker + ", char=" + String.fromCharCode(marker) +")."};
            this.close();
        }
    }

    if(callback != null) {
        callback(response);
    }

}

/*
    Used internally to work on new data arrival.

*/
JBNode.prototype.processBuffer = function() {
    while(this.isFullResponse()) {
        this.readResponse();
    }
}

