var sys = require('sys');
var jb = require('./jbnode');

(function() {

    const jobCount = 100;
    var results = 0;
    var intervalId;

    var pollResult = function(response) {
        if (response.error) {
            sys.debug("Error: " + response.message);
        } else {
            if (response.data.length > 0) {
                sys.debug("'" + response.data + "' is done!");
                if (++results == jobCount) {
                    clearInterval(intervalId);
                    jbNode.close();
                }
            }
        }
    }

    var submitJobsAndStartPolling = function() {
        for (var x = 0; x < jobCount; x++) {
            sys.debug("Submitting job#" + x);

            jbNode.call("workerService.submit", "job#" + x);
        }

        intervalId = setInterval(function() {
            jbNode.call("workerService.poll", null, pollResult)
        }, 100);
    };

    var host = process.argv[2];
    var port = process.argv[3];
    var jbNode = new jb.JBNode(port, host);
    
    jbNode.on("connect", submitJobsAndStartPolling);
    jbNode.connect();

})(); 