package net.karmafiles.jbnode;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import java.util.logging.Logger;

/**
 * Created by Ilya Brodotsky
 * Date: 15.09.2010
 * Time: 18:59:08
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class RequestDecoder extends FrameDecoder {

    private static final Logger logger = Logger.getLogger(RequestDecoder.class.getName());

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        if(buffer.readableBytes() < 4) {
            return null;
        }

        int headerLength = buffer.getInt(buffer.readerIndex());

        if(buffer.readableBytes() < (4 + headerLength + 4)) {
            return null;
        }

        int bodyLength = buffer.getInt(buffer.readerIndex() + 4 + headerLength);

        if(buffer.readableBytes() < (4 + headerLength + 4 + bodyLength)) {
            return null;
        }

        Request request = new Request();

        buffer.skipBytes(4); // skip header length int

        byte[] headerBuffer = new byte[headerLength];
        buffer.readBytes(headerBuffer);
        String header = new String(headerBuffer);

        if (header.length() < Constants.MIN_HEADER_LENGTH) {
            throw new JBNodeException("Protocol error: invalid header (1).");
        }

        String[] headerParts = header.split("\\.");
        if (headerParts.length != 2) {
            throw new JBNodeException("Protocol error: invalid header (2).");
        }

        request.setService(headerParts[0]);
        request.setMethod(headerParts[1]);

        buffer.skipBytes(4);

        request.setData(new byte[bodyLength]);
        buffer.readBytes(request.getData());

        return request;
    }

}
