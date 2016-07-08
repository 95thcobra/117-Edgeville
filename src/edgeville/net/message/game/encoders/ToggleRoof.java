package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import io.netty.buffer.Unpooled;

/**
 * Custom command, toggles roofs.
 * @author Simon
 *
 */
public class ToggleRoof implements Command {

	private int value;

	public ToggleRoof(int value) {
		this.value = value;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(Unpooled.buffer());
		buffer.packet(10000);
		return buffer;
	}
}
