package net.karmafiles.jbnode;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * Created by Ilya Brodotsky
 * Date: 22.09.2010
 * Time: 14:59:49
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class ResponseEncoder extends SimpleChannelHandler {
    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if(!(e.getMessage() instanceof Response)) {
            throw new JBNodeException("'Response' object expected.");
        }

        Response response = (Response)e.getMessage();

        ChannelBuffer responseBuffer1 = ChannelBuffers.buffer(4 + 1);

        int resultLength = response.getData().length;
        responseBuffer1.writeInt(resultLength);

        byte resultByte = response.isError() ? (byte) '!' : (byte) '.';
        responseBuffer1.writeByte(resultByte);

        ChannelBuffer responseBuffer2 = ChannelBuffers.wrappedBuffer(response.getData());

        Channels.write(ctx, e.getFuture(), responseBuffer1);
        Channels.write(ctx, e.getFuture(), responseBuffer2);
    }
}
