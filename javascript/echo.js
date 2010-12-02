var sys = require('sys');
var jb = require('./jbnode');

/*
    Send and receive unicode string.

 */
(function(){

    var unicodeString = "Hello, world!"

    var processResponse = function(response) {
        sys.debug("Service responded: " + response.data);

        jbNode.close();
    }

    var connect = function() {
        jbNode.call("echoService.echo", unicodeString, processResponse);
    };

    var jbNode = new jb.JBNode(8888, "localhost");
    
    jbNode.on("connect", connect);
    jbNode.connect();

})(); 