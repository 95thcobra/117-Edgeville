package edgeville.net.codec.game;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.net.ServerHandler;
import edgeville.net.message.game.encoders.Command;

/**
 * @author Simon on 8/22/2014.
 */
@ChannelHandler.Sharable
public class CommandEncoder extends MessageToByteEncoder<Command> {

	private static final Logger logger = LogManager.getLogger(CommandEncoder.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, Command msg, ByteBuf out) throws Exception {
		//System.out.println("MESSAGE SENT: "+msg.toString());

		Player player = ctx.channel().attr(ServerHandler.ATTRIB_PLAYER).get();
		RSBuffer buffer = msg.encode(player);
		buffer.finish();

		if (buffer.packet() != -1) {
			out.writeByte((byte) buffer.packet()/* + (byte)player.outrand().nextInt()*/);
			out.writeBytes(buffer.get());
		}

		buffer.get().release();
	}

}
