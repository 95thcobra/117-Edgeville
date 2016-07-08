package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;

/**
 * @author Simon on 8/11/2015.
 */
public class NpcOnInterface implements Command {

	private int hash;
	private int npc;

	public NpcOnInterface(int target, int targetChild, int npc) {
		hash = (target << 16) | targetChild;
		this.npc = npc;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(7));
		buffer.packet(105);

		buffer.writeIntV2(hash);
		buffer.writeLEShortA(npc);
		return buffer;
	}

}
