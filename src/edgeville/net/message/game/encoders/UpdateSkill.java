package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;

/**
 * @author Simon on 8/23/2014.
 */
public class UpdateSkill implements Command {

	private int skill;
	private int level;
	private int xp;

	public UpdateSkill(int skill, int level, int xp) {
		this.skill = skill;
		this.level = level;
		this.xp = xp;
	}

	@Override public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(7));

		buffer.packet(167);

		buffer.writeByteS(level);
		buffer.writeIntV2(xp);
		buffer.writeByteA(skill);

		return buffer;
	}
}
