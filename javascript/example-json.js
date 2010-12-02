var sys = require('sys');
var jb = require('./jbnode');

(function(){

    var json = {
        name: "test1",
        id: 1,
        nodes: [
            {
                name: "test2",
                id: 2
            },
            {
                name: "test3",
                id: 3
            }
        ]
    }

    var printResponse = function(response) {
        if(response.error) {
            sys.debug("Error: " + response.message);
        } else {
            sys.debug("Received: " + response.data);
            jbNode.close();
        }
    }

    var connect = function() {
        var data = JSON.stringify(json);
        sys.debug("Sending: " + data);
        jbNode.call("jsonService.test", data, printResponse);
    };

    var host = process.argv[2];
    var port = process.argv[3];
    var jbNode = new jb.JBNode(port, host);
    
    jbNode.on("connect", connect);
    jbNode.connect();

})(); 