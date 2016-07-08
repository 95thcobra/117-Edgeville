package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import io.netty.buffer.Unpooled;

/**
 * @author Simon on 8/22/2014.
 */
public class ChangeMapVisibility implements Command {

	private int state;

	public ChangeMapVisibility(int state) {
		this.state = state;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(Unpooled.buffer());
		buffer.packet(106);
		buffer.writeByte(state);
		return buffer;
	}
}
