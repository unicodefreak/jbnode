var sys = require('sys');
var events = require("events");
var jb = require('./jbnode');
var assert = require('assert');

/*
    Send and receive unicode string.

 */
(function(){

    var unicodeString = "Hello, world! Привет, мир! 世界您好!"

    var processResponse = function(response) {
        if(response.error) {
            sys.debug("Error! The message is '" + response.message + "'");
        } else {
            if(response.data == unicodeString) {
                sys.debug("OK.");
            } else {
                sys.debug("Test failed.");
            }
        }

        jbNode.close();
    }

    var connect = function() {
        jbNode.call("echoService.echo", unicodeString, processResponse);
    };

    var host = process.argv[2];
    var port = process.argv[3];
    var jbNode = new jb.JBNode(port, host);
    
    jbNode.on("connect", connect);
    jbNode.connect();

})(); 