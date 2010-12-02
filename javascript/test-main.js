var sys = require('sys');
var events = require("events");
var jb = require('./jbnode');
var assert = require('assert');

/*
    Basic test

 */
(function(){
    var calls = 0;
    var testData = [];
    var responses = 0;
    var ticker = new events.EventEmitter();
    var dataSent = 0;
    var dataReceived = 0;

    const EMPTY_CALLS = 1;
    const SHORT_CALLS = 100;
    const LONG_CALLS = 1000;
    const EXTRA_LONG_CALLS = 1100;
    const TOTAL_CALLS = EXTRA_LONG_CALLS;

    var processResponse = function(response) {
        responses++;

        if(response.error) {
            sys.debug("Error! The message is '" + response.message + "'");
        } else {
            dataReceived += response.data.length;

            var testString = testData.shift();
            if(testString != response.data) {
                sys.debug("Must be: " + testString);
                sys.debug("Got: " + response.data);
                sys.debug("Test failed: Got wrong response.");
            }

            if(responses >= TOTAL_CALLS) {
                sys.debug("Got " + responses + " response(s). " + dataReceived + " byte(s) of data received. " + testData.length + " response(s) remain.");
                jbNode.close();
                sys.debug("finished.");
            }
        }

    }

    ticker.addListener('tick', function() {
        var self = this;

        process.nextTick(function(){
            calls++;
            var testString = "";

            if(calls == 0) {
                sys.debug("testing empty value...");
            } else if(calls == EMPTY_CALLS) {
                sys.debug("testing short values...");
            } else if(calls == SHORT_CALLS) {
                sys.debug("testing long values...");
            } else if(calls == LONG_CALLS) {
                sys.debug("testing extra long values...");
            }

            var size = (calls < LONG_CALLS) ? calls : calls * 10;
            for(var x = 0; x < size; x++) {
                testString += "#" + x + " 0123456789 abcdef ";
            }
            testString = "@" + calls + " " + testString;
            testData.push(testString);

            jbNode.call('echoService.echo', testString, processResponse);

            dataSent += testString.length;

            if(calls < TOTAL_CALLS) {
                ticker.emit("tick");
            } else {
                sys.debug("Called " + calls + " time(s). " + dataSent + " byte(s) of data sent.");
            }

        });
    });

    var connect = function() {
        sys.debug("connected, start ticking...");
        ticker.emit('tick');
    };

    var host = process.argv[2];
    var port = process.argv[3];
    var jbNode = new jb.JBNode(port, host);

    jbNode.on("connect", connect);
    jbNode.connect();

})(); 