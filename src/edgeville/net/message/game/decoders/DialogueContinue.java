package edgeville.net.message.game.decoders;

import edgeville.aquickaccess.dialogue.DialogueAction;
import edgeville.io.RSBuffer;
import edgeville.model.AttributeKey;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Privilege;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon on 5-2-2015.
 */
@PacketInfo(size = 6)
public class DialogueContinue implements Action {

	private int hash;
	private int slot;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		hash = buf.readLEInt();
		slot = buf.readULEShortA();

		if (slot == 0xFFFF)
			slot = -1;
	}

	@Override
	public void process(Player player) {
		if (player.getPrivilege().eligibleTo(Privilege.ADMIN) && player.isDebug())
			player.message("Dialogue [%d:%d], slot: %d", hash >> 16, hash & 0xFFFF, slot);

		int id = hash >> 16;
		int child = hash & 0xFFFF;

		// Object returnval = null;
		int returnval = -1;
		if (id == 219) {
			returnval = slot;
		}

		player.message("Dialogue action: id:" + id + " child:" + child + " returnval:" + returnval);
		new DialogueAction(player, returnval).handleDialog();
		// player.world().server().scriptExecutor().continueFor(player,
		// WaitReason.DIALOGUE, returnval);
	}
}
