package edgeville.net.codec.pregame;

import edgeville.net.message.Js5DataMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author Simon on 8/22/2014.
 *
 */
public class Js5DataEncoder extends MessageToByteEncoder<Js5DataMessage> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Js5DataMessage msg, ByteBuf out) throws Exception {
		byte[] data = msg.data();

		out.writeByte(msg.index());
		out.writeShort(msg.container());

		for (byte b : data) {
			if (out.writerIndex() % 512 == 0) {
				out.writeByte(-1);
			}

			out.writeByte(b);
		}
	}
}
