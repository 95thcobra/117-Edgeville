package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;

/**
 * @author Simon on 8/11/2015.
 */
public class PlayerOnInterface implements Command {

	private int hash;

	public PlayerOnInterface(int target, int targetChild) {
		hash = (target << 16) | targetChild;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(5));
		buffer.packet(172);

		buffer.writeIntV1(hash);
		return buffer;
	}

}
