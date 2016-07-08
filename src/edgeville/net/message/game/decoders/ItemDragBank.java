package edgeville.net.message.game.decoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon
 */
@PacketInfo(size = 16)
public class ItemDragBank implements Action {

	private int itemId;
	private int itemOther;
	private int slotOther;
	private int slot;
	private int hash1;
	private int hash2;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		// TODO Auto-generated method stub
		itemId = buf.readULEShortA();
		itemOther = buf.readULEShortA();
		slotOther = buf.readUShort();
		slot = buf.readUShort();
		hash1 = buf.readIntV2();
		hash2 = buf.readIntV2();
	}

	@Override
	public void process(Player player) {
		player.messageDebug("id:%d, itemother:%d, slotother:%d, slot:%d", itemId, itemOther, slotOther, slot);
		
		int hashthing1 = hash1>>16;
		int hashthing12 = hash1&0xFFFF;
		
		int hashthing2 = hash2>>16;
		int hashthing22 = hash2&0xFFFF; // 10 if tab, 12 if item
		player.messageDebug("%d,%d,%d,%d", hashthing1, hashthing12, hashthing2, hashthing22);
		
		player.getBank().moveItemOnItem( itemId,  slot,  itemOther, slotOther, hashthing22, hashthing12);
	}

}
