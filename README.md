#jbNode - fast calls from node.js to Java 

##About

jbNode ("java-bridge-node") is a simple client/server framework supposed to provide a fast and simple access to Java services from node.js.

There are many applications that can benefit from lightweight and hyper-fast node.js architecture. There are also many development issues that can't be solved using pure JavaScript and node.js API.
The examples are: logging to the external system, state synchronization, blocking and heavy tasks (sending emails), operations with large amounts of data (remote file retrieval), integration with 3rd parties using complex interface (payment gateways), usage of complicated code that is difficult to reimplement (RDBMS access, proprietary format parsing).

One of solutions is to create runtime modules. It may be the best way to deal with preexisting C/C++ code, but in most cases maintenance and development of such modules will be difficult for web service developer. Others include spawning of child processes or communication with server systems using network streams.

jbNode is an attempt to create simple and generic network interface between node.js and Java. On the Java side the [JBoss Netty](http://www.jboss.org/netty) (very fast socket server built with [Java NIO](http://download.oracle.com/javase/6/docs/api/index.html?java/nio/channels/package-summary.html)) is used.

To call Java code from node.js using jbNode you have to:

* create Java class with methods accepting and returning byte arrays
* run jbNode instance as a standalone server or embedded service and register your service class within the server 
* establish the connection to jbNode server using provided JavaScript client for node.js
* register result/error listeners and start executing asynchronous calls from JavaScript to Java service

High throughput is achieved by eliminating the need to parse complex protocol data (data overhead is about 20 bytes for request/response pair, data is passed in binary form) and high overall performance of Netty and node.js. 

Byte array parameters are chosen for maximum binary compatibility. Service class may be extended to act as "request router", determining the final destination from parsed request data, so you may pass whatever data format you want.

## Running jbNode in standalone mode:

1. Install [node.js](http://nodejs.org/)
2. Install [Maven](http://maven.apache.org/)
3. Clone the jbNode source, cd to that directory
4. `cd java && maven package assembly:assembly`
5. If were no errors, jbNode is now built in `./target/jbNode-assembly-1.0-package`
6. Run `./run.sh --host localhost --port 8888 --modules=echo`

You will see some log messages and finally the line will appear:
<pre>INFO: Listening on [localhost:8888]</pre>
If you see any Java exception, please read an error message. It will be clear if some kind of bind or configuration error occurred.

##The simplest example: 

<pre>
var sys = require('sys');
var jb = require('./jbnode');

(function(){
    var s = "Hello, world!"

    var processResponse = function(response) {
       sys.debug("Service responded: " + response.data);

       jbNode.close();
    }

    var connect = function() {
       jbNode.call("echoService.echo", s, processResponse);
    };

    var jbNode = new jb.JBNode(8888, "localhost");
   
    jbNode.on("connect", connect);
    jbNode.connect();

})();
</pre>

Navigate to 'javascript' dir and run the code using the command: 
<pre>
~# node ./echo.js
</pre>
Voila! You have got the message back from Java service.

##Running examples

jbNode comes with a handful of examples demonstrating different usage scenarios.

To execute an example, run jbNode server with all example modules enabled: 

<pre>./run.sh --host localhost --port 8888 --modules=echo,json,persist,workers</pre>

Cd to the directory you cloned jbNode to and run:

<pre>~# node example.js &lt;HOST&gt; &lt;PORT&gt; </pre> 

###Echo example

The simplest scenario possible. Code for node.js is self-descriptive. The Java side (which is in ./java/examples/echo/src/main/java/net/karmafiles/jbnode/examples/echo/Echo.java) is also very simple:

<pre>
public class Echo {

    public final static String jbNodeVersion = "1.0";

    public byte[] echo(byte[] param) {
        return param;
    }

    public byte[] echoError(byte[] param) {
        throw new RuntimeException(new String(param));
    }

}
</pre>

Any method executed by remote JavaScript code accepts byte array, the byte[] is also returned. 

###JSON example

Passing JSON to Java service and back

###Persistence example

Using Java backend for RDBMS persistence.

###Workers

Passing jobs to multiple Java workers (created with java.util.concurrent) and poll for results. 

##jbNode architecture and Netty

jbNode is implemented on the top of JBoss Netty server. The 'core' module contains all the code needed to run server. 

The 'spring' module contains sample descriptors needed to initialize jbNode in Spring environment. Module 'bootstrap', which is used to start standalone instance, is an example of initialization through Spring context. 

##Embedding jbNode

Build jbNode using <pre>mvn install</pre> command, include <pre>&lt;artifactId&gt;jbnode-core&lt;/artifactId&gt;</pre> in your Maven dependencies, instantiate JBNode class, set 'host' and 'port' fields. Use 'configure' method to attach a service. Call 'start' to bind the server. 

##Writing modules

Use one of examples to start your own module. Don't forget to add a new module to '--modules' command-line parameter. 

##Running tests

If something is broken, run tests to learn what exactly. 

###test-composite-buffer.js

CompositeBuffer, declared in jbnode.js, is a custom feature (based on node Buffers) used to treat incoming and outgoing "packet" fragmentation. 

The reason for creating a secondary buffering mechanism is simple: you can never know if 'incoming data' event was triggered on full message, or on the single byte or on hundreds of messages already waiting to be fetched. The outgoing buffers are needed to prevent the internal OS buffer from overflow. If application buffering is not used, the process may behave weird and in result hang or crash. If outgoing data is buffered within the application, the app will simply terminate with out-of-memory error. 

###test-errors.js

Test if server-side errors are correctly delivered. 

###test-main.js

A simple "load" test. 

###test-unicode.js

Passing unicode strings.

##Contacts

Developer: [partorg.dunaev@gmail.com](mailto:partorg.dunaev@gmail.com)