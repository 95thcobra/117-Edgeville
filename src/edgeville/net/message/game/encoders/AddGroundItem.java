package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.GroundItem;
import edgeville.model.entity.Player;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon on 8/22/2015.
 */
public class AddGroundItem implements Command {

	private GroundItem item;

	public AddGroundItem(GroundItem item) {
		this.item = item;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer packet = new RSBuffer(player.channel().alloc().buffer(6)).packet(145);

		packet.writeLEShortA(item.item().getId());
		int x = item.tile().x % 8;
		int z = item.tile().z % 8;
		packet.writeByteA((x << 4) | z);
		packet.writeLEShort(item.item().getId());

		return packet;
	}

}

