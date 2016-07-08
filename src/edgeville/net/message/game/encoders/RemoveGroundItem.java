package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.GroundItem;
import edgeville.model.entity.Player;

/**
 * @author Simon on 8/22/2015.
 */
public class RemoveGroundItem implements Command {

	private GroundItem item;

	public RemoveGroundItem(GroundItem item) {
		this.item = item;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer packet = new RSBuffer(player.channel().alloc().buffer(6)).packet(244);

		packet.writeByteN(((item.tile().x % 8) << 4) | (item.tile().z % 8));
		packet.writeShortA(item.item().getId());

		return packet;
	}

}

