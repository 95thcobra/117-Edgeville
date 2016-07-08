package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;

/**
 * @author Simon on 8/22/2014.
 */
public class OpenInterface implements Command {

	private int id;
	private boolean autoclose;
	private int target;
	private int targetChild;

	public OpenInterface(int id, int target, int targetChild, boolean autoclose) {
		this.id = id;
		this.target = target;
		this.targetChild = targetChild;
		this.autoclose = autoclose;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(8));

		buffer.packet(193);

		buffer.writeLEShortA(id);
		buffer.writeByteS(autoclose ? 1 : 0);
		buffer.writeLEInt((target << 16) | targetChild);

		return buffer;
	}

}
