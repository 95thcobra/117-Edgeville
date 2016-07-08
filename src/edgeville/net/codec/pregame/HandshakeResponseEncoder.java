package edgeville.net.codec.pregame;

import edgeville.net.message.HandshakeResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author Simon on 8/4/2014.
 */
public class HandshakeResponseEncoder extends MessageToByteEncoder<HandshakeResponse> {

	@Override
	protected void encode(ChannelHandlerContext ctx, HandshakeResponse msg, ByteBuf out) throws Exception {
		out.writeByte(msg.value());
	}

}
