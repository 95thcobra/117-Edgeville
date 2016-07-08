package edgeville.net.message.game.decoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.interfaces.inputdialog.StringInputDialog;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.InvokeScript;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Tom on 11/16/2015.
 */
@PacketInfo(size = -1)
public class StringInput implements Action {
	private Player player;

	public StringInput(Player player) {
		this.player = player;
	}

	private String value;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		value = buf.readString();
	}

	@Override
	public void process(Player player) {
		if (player.getLastInputDialog() != null)
			((StringInputDialog)player.getLastInputDialog()).doAction(value);
	}
}
