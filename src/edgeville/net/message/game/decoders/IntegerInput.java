package edgeville.net.message.game.decoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.interfaces.inputdialog.NumberInputDialog;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon on 2-7-2016.
 */
@PacketInfo(size = 4)
public class IntegerInput implements Action {

	private int value;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		value = buf.readInt();
	}

	@Override
	public void process(Player player) {
		if (player.getLastInputDialog() != null) {
			((NumberInputDialog) player.getLastInputDialog()).doAction(value);
		}
	}
}
