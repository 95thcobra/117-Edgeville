package edgeville.net.message.game.decoders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.combat.magic.SpellOnTargetAction;
import edgeville.io.RSBuffer;
import edgeville.model.AttributeKey;
import edgeville.model.entity.Npc;
import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon on 8/20/2015.
 */
@PacketInfo(size = 9)
public class SpellOnNpc implements Action {

	private static final Logger logger = LogManager.getLogger(SpellOnPlayer.class);

	private int idk1;
	private int hash;
	private int idk3;
	private int npcIndex;
	private int interfaceId;
	private int interfaceChildId;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {

		idk1 = buf.readULEShort();
		hash = buf.readIntV1();
		idk3 = buf.readByte();

		npcIndex = buf.readUShortA();
		interfaceId = (hash >> 16);
		interfaceChildId = hash & 0xFFFF;
	}

	@Override
	public void process(Player player) {
		player.messageDebug("SPELLONNPC:" + idk1 + "," + hash + "," + idk3 + "," + npcIndex);
		player.messageDebug("SPELLONNPCBITSHIFT:" + (idk1 >> 16) + "," + (hash >> 16) + "," + (idk3 >> 16) + "," + (npcIndex >> 16) + "," + interfaceChildId);

		player.stopActions(false);

		Npc other = player.world().npcs().get(npcIndex);
		if (other == null) {
			player.message("Unable to find npc.");
		} else {
			if (!player.locked() && !player.dead() && !other.dead()) {
				player.face(other);
				player.putAttribute(AttributeKey.TARGET, hash);
				new SpellOnTargetAction(player, other, interfaceId, interfaceChildId).start(false);
			}
		}
	}
}
