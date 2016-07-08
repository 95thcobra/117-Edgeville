package edgeville.net.message.game.decoders;

import io.netty.channel.ChannelHandlerContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.combat.magic.SpellOnTargetAction;
import edgeville.io.RSBuffer;
import edgeville.model.AttributeKey;
import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;

/**
 * @author Simon on 8/20/2015.
 */
@PacketInfo(size = 9)
public class SpellOnPlayer implements Action {

	private static final Logger logger = LogManager.getLogger(SpellOnPlayer.class);

	private int slot;
	private int targetIndex;
	private int interfaceId;
	private int child;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		slot = buf.readUShort();
		targetIndex = buf.readULEShort();
		int hash = buf.readIntV1();

		interfaceId = hash >> 16;
		child = hash & 0xFFFF;
		boolean run = buf.readByte() == 1;
	}

	@Override
	public void process(Player player) {
		player.messageDebug(slot + "," + targetIndex + "," + interfaceId + "," + child);

		player.stopActions(false);

		Player other = player.world().players().get(targetIndex);
		if (other == null) {
			player.message("Unable to find player.");
			return;
		}
		if (other.inCombat() && player.getTarget() != other && player.getLastAttackedBy() != other) {
			player.message("This player is in combat.");
			return;
		}
		if (!player.locked() && !player.dead() && !other.dead()) {
			player.face(other);
			player.putAttribute(AttributeKey.TARGET, targetIndex);
			new SpellOnTargetAction(player, other, interfaceId, child).start(false);
		}
	}

}
