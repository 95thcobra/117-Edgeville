package edgeville.net.message.game.decoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon on 8/23/2014.
 */
@PacketInfo(size = 0)
public class PingServer implements Action {

	@Override
	public void process(Player player) {
		/* Pinged, wow. */
	}

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		/* Empty packet */
	}
}
