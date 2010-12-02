package net.karmafiles.jbnode;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Ilya Brodotsky
 * Date: 01.09.2010
 * Time: 18:27:58
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class JBNode {

    private String host;
    private Integer port;
    private boolean debugModeEnabled = false;
    private static final Logger logger = Logger.getLogger(JBNode.class.getName());
    private ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
    private boolean tcpNoDelay = true;
    private boolean keepAlive = true;

    public JBNode() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean isDebugModeEnabled() {
        return debugModeEnabled;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void configure(String name, Object service) throws JBNodeException {
        serviceConfiguration.configure(name, service);     
    }

    private ChannelGroup channelGroup = new DefaultChannelGroup("jbNode-channels");

    public void start() throws JBNodeException {
        if(host == null || port == null || "".equals(host.trim()) || port == 0) {
            throw new JBNodeException("Both host and port must be specified.");
        }

        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool(), 10));



        ChannelPipelineFactory channelPipelineFactory = new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new RequestDecoder(),

                        new ServerHandler() {
                            @Override
                            protected Object getService(String serviceName) {
                                return serviceConfiguration.getService(serviceName);
                            }

                            @Override
                            protected Method getServiceMethod(String serviceName, String methodName) {
                                return serviceConfiguration.getServiceMethod(serviceName, methodName);
                            }

                            @Override
                            protected byte[] executeMethod(String serviceName, String methodName, Object service,
                                                           Method serviceMethod, byte[] data) throws Exception {
                                Object result = serviceMethod.invoke(service, data);
                                if (result != null) {
                                    return (byte[]) result;
                                } else {
                                    return new byte[]{};
                                }
                            }

                            @Override
                            protected boolean isDebugMode() {
                                return isDebugModeEnabled();
                            }
                        },

                        new ResponseEncoder()
                );
            }
        };
        
        bootstrap.setPipelineFactory(channelPipelineFactory);

        bootstrap.setOption("tcpNoDelay", isTcpNoDelay());
        bootstrap.setOption("keepAlive", isKeepAlive());

        try {
            logger.log(Level.INFO, "Binding to [" + getHost() + ":" + getPort() + "]...");

            Channel channel = bootstrap.bind(new InetSocketAddress(InetAddress.getByName(host), port));
            logger.log(Level.INFO, "Listening on [" + getHost() + ":" + getPort() + "].");
            channelGroup.add(channel);

        } catch (UnknownHostException e) {
            throw new JBNodeException("Unknown host '" + host + "'", e);
        } catch (Throwable t) {
            throw new JBNodeException("Can't bind to '" + host + ":" + port + "'", t);
        }
    }

    public void shutdown() {
        logger.log(Level.INFO, "Shutdown was called.");
        if(channelGroup != null) {
            ChannelGroupFuture future = channelGroup.close();
            future.awaitUninterruptibly();
        }
        logger.log(Level.INFO, "Shutdown complete.");
    }
}
