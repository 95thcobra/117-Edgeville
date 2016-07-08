package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.util.SettingsBuilder;

/**
 * @author Simon on 8/11/2015.
 */
public class InterfaceText implements Command {

	private int hash;
	private String text;

	public InterfaceText(int target, int targetChild, String text) {
		hash = (target << 16) | targetChild;
		this.text = text;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(text.length() + 1 + 1 + 4));
		buffer.packet(55).writeSize(RSBuffer.SizeType.SHORT);

		buffer.writeString(text);
		buffer.writeIntV2(hash);
		return buffer;
	}
}