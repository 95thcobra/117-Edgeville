package edgeville.net.message.game.decoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon on 8/23/2014.
 *
 */
@PacketInfo(size = 1)
public class WindowStateChanged implements Action {

	private boolean visible;

	@Override public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		visible = buf.readByte() == 1;
	}

	@Override public void process(Player player) {
		/* We could register this for antibotting :D */
	}
}
