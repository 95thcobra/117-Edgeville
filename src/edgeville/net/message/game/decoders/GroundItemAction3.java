package edgeville.net.message.game.decoders;

import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.io.RSBuffer;
import edgeville.model.GroundItem;
import edgeville.model.entity.PathQueue;
import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon on 8/23/2015.
 */
@PacketInfo(size = 7)
public class GroundItemAction3 implements Action {

	private int x;
	private int z;
	private int id;
	private boolean run;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		z = buf.readUShort();
		run = buf.readByteS() == 1;
		id = buf.readUShort();
		x = buf.readUShort();
	}

	@Override
	public void process(Player player) {
		GroundItem item = player.world().getGroundItem(x, z, player.getTile().level, id);

		if (!player.locked() && !player.dead()) {
			//player.putattrib(AttributeKey.GROUNDITEM_TARGET, item);
			//player.world().server().scriptExecutor().executeScript(player, GroundItemTaking.script);
			takeGroundItem(player, item);
		}
	}

	private void takeGroundItem(Player player, GroundItem item) {
		if (item == null) {
			return;
		}

		player.walkTo(item.tile().x, item.tile().z, PathQueue.StepType.REGULAR);
		player.world().getEventHandler().addEvent(player, new Event() {
			@Override
			public void execute(EventContainer container) {
				if (item.tile().equals(player.getTile())) {
					if (player.world().groundItemValid(item) && (player.getInventory().add(item.item(), false).success())) {
						player.world().removeGroundItem(item);
						player.sound(2582, 0);
					}
				}
			}
		});
	}

}
