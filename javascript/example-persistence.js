var sys = require('sys');
var jb = require('./jbnode');

(function(){

    var printUsers = function(response) {
        if(response.error) {
            sys.debug("Error: " + response.message);
        } else {
            sys.debug(response.data);
            jbNode.close();
        }
    }

    var listUsers = function(response) {
        if(response.error) {
            sys.debug("Error: " + response.message);
        } else {
            jbNode.call("persistenceService.listUsers", null, printUsers);
        }
    }

    var saveUsers = function() {
        jbNode.call("persistenceService.saveUser", JSON.stringify({ login: "peter"}));
        jbNode.call("persistenceService.saveUser", JSON.stringify({ login: "dude"}));
        jbNode.call("persistenceService.saveUser", JSON.stringify({ login: "cat22"}), listUsers);
    };

    var host = process.argv[2];
    var port = process.argv[3];
    var jbNode = new jb.JBNode(port, host);

    jbNode.on("connect", saveUsers);
    jbNode.connect();

})(); 