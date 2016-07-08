package edgeville.net.message.game.decoders;

import edgeville.aquickaccess.actions.ItemOption1;
import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.model.item.Item;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon.
 */
@PacketInfo(size = 8)
public class ItemAction1 extends ItemAction {

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		slot = buf.readUShortA();
		item = buf.readUShort();
		hash = buf.readLEInt();
	}

	@Override
	protected int option() {
		return 0;
	}

	@Override
	public void process(Player player) {
		super.process(player);

		Item item = player.getInventory().get(slot);
		if (item != null && item.getId() == this.item && !player.locked() && !player.dead()) {
			player.stopActions(false);
			//player.world().server().scriptRepository().triggerItemOption1(player, item.id(), slot);
			new ItemOption1(player, item.getId(), slot).start();
		}
	}
}
