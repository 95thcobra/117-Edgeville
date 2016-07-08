package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import io.netty.buffer.Unpooled;

public class UpdateGame implements Command {

	private int value;

	public UpdateGame(int value) {
		this.value = value;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(Unpooled.buffer());

		buffer.packet(135);
		buffer.writeShort(value);

		return buffer;
	}
}
