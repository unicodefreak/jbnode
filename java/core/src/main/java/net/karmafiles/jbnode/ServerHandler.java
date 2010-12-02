package net.karmafiles.jbnode;

import org.jboss.netty.channel.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Ilya Brodotsky
 * Date: 01.09.2010
 * Time: 17:08:42
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

/*
    Performs message processing and executes configured services opon received data
 */
public abstract class ServerHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = Logger.getLogger(ServerHandler.class.getName());

    /*
        Get configured service 
     */

    protected abstract Object getService(String serviceName);

    /*
        Get configured method
     */

    protected abstract Method getServiceMethod(String serviceName, String methodName);

    /*
        Executes desired method
     */
    protected abstract byte[] executeMethod(String serviceName, String methodName, Object service,
                                            Method serviceMethod, byte[] data) throws Exception;

    /*
        Do we need stacktraces?
     */
    protected abstract boolean isDebugMode();

    /*
        React on transport event. This is not actually needed now... 
     */
    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        super.handleUpstream(ctx, e);
    }

    /*
        All message processing and code execution happens here.

        The message is validated and read by RequestDecoder (the first chain in the pipeline),
        processed by messageReceived method and the result is written back to channel by
        ResponseEncoder (the last chain in the pipeline).   
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent messageEvent) throws JBNodeException {

        if(!(messageEvent.getMessage() instanceof Request)) {
            throw new JBNodeException("Message contains no 'Request' object.");
        }

        Request request = (Request)messageEvent.getMessage();

        Object service = getService(request.getService());
        Method method = getServiceMethod(request.getService(), request.getMethod());

        if (service == null || method == null) {
            logger.log(Level.WARNING, "Service '" + request.getService() + "' or method " +
                "'" + request.getService() + "." + request.getMethod() + "' is not configured.");

            throw new JBNodeException("Protocol error: invalid header (3).");
        }

        byte[] resultBody;
        boolean executionFailed = false;
        
        try {
            try {
                resultBody = executeMethod(request.getService(), request.getMethod(), service, method, request.getData());
            } catch (IllegalAccessException e) {
                throw e;
            } catch (InvocationTargetException e) {
                if (e.getCause() != null) {
                    throw e.getCause();
                } else {
                    throw new Exception("Unknown exception thrown by service method.");
                }
            }
        } catch (Throwable t) {
            if (isDebugMode()) {
                logger.log(Level.SEVERE, "Exception thrown " + "'" + request.getService() +
                        "." + request.getMethod() + "' " + t.getMessage(), t);
            } else {
                logger.log(Level.SEVERE, "Exception thrown " + "'" + request.getService() +
                        "." + request.getMethod() + "' " + t.getMessage());
            }

            executionFailed = true;
            resultBody = t.getMessage().getBytes();
        }
        
        messageEvent.getChannel().write(new Response(executionFailed, resultBody));
        
    }

    /*
        Close the channel in case of internal error
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.log(
                Level.WARNING,
                "Unexpected exception from downstream.",
                e.getCause());

        e.getChannel().close();
    }                                   
}

