package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;

/**
 * @author Simon on 8/11/2015.
 */
public class AnimateInterface implements Command {

	private int hash;
	private int anim;

	public AnimateInterface(int target, int targetChild, int anim) {
		hash = (target << 16) | targetChild;
		this.anim = anim;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(7));
		buffer.packet(14);

		buffer.writeIntV2(hash);
		buffer.writeShort(anim);
		return buffer;
	}

}
