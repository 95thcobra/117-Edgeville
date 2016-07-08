package edgeville.net.message.game.decoders;

import edgeville.io.RSBuffer;
import edgeville.model.AttributeKey;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Privilege;
import edgeville.model.item.Item;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon on 8/11/2015.
 */
@PacketInfo(size = 9)
public class ItemDragAction implements Action {

	private int from;
	private int to;
	private boolean insert;
	private int hash;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		from = buf.readUShort();
		insert = buf.readUByte() == 1;
		to = buf.readULEShort();
		hash = buf.readIntV2();
	}

	@Override
	public void process(Player player) {
		if (player.getPrivilege().eligibleTo(Privilege.ADMIN) && player.isDebug())
			player.message("Drag: from=%d to=%d inter=%d child=%d insert=%b", from, to, hash>>16, hash&0xFFFF, insert);

		int inter = hash >> 16;
		if (inter == 149) {
			Item old = player.getInventory().get(to);
			player.getInventory().set(to, player.getInventory().get(from));
			player.getInventory().set(from, old);
		}
	}

}
