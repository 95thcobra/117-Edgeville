package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;

/**
 * @author Simon on 8/22/2014.
 */
public class SetVarp implements Command {

	private int id;
	private int value;
	private boolean small;

	public SetVarp(int id, int value) {
		this.id = id;
		this.value = value;
		small = value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(small ? 4 : 7));
		buffer.packet(small ? 47 : 95);

		if (small) {
			buffer.writeShortA(id);
			buffer.writeByte(value);
		} else {
			buffer.writeIntV1(value);
			buffer.writeLEShortA(id);
		}

		return buffer;
	}
}
