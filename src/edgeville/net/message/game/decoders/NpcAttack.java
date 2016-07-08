package edgeville.net.message.game.decoders;

import edgeville.combat.Combat;
import edgeville.combat.PvMCombat;
import edgeville.io.RSBuffer;
import edgeville.model.AttributeKey;
import edgeville.model.World;
import edgeville.model.entity.Npc;
import edgeville.model.entity.PathQueue;
import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Tom on 9/26/2015. Modified by Sky on 1/3/2016.
 */
@PacketInfo(size = 3)
public class NpcAttack implements Action {

	private int size = -1;
	private int opcode = -1;
	private boolean run;
	private int index;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		run = buf.readByteA() == 1;
		index = buf.readUShortA();
	}

	@Override
	public void process(Player player) {
		player.stopActions(true);

		// player.message("npc attack --- npcindex:" + index + " run:" + run);
		Npc other = player.world().npcs().get(index);

			player.messageDebug("npcid:" + other.id() + " run:" + run);

		if (!player.locked() && !player.dead() && !other.dead()) {
			// player.stepTowards(other, 20);
			player.face(other);

			player.putAttribute(AttributeKey.TARGET_TYPE, 1);
			player.putAttribute(AttributeKey.TARGET, /* index */ other);
			other.putAttribute(AttributeKey.LAST_ATTACKED_BY, player);

			// player.world().server().scriptExecutor().executeScript(player,
			// PlayerCombat.script);
			new PvMCombat(player, other).start();
		}
	}
}
