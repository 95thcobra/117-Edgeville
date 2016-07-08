package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;

/**
 * @author Simon on 8/22/2014.
 */
public class SetRootPane implements Command {

	private int paneId;

	public SetRootPane(int paneId) {
		this.paneId = paneId;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(4));

		buffer.packet(32);
		buffer.writeShortA(paneId);
		System.out.println("sending root!" + paneId);

		return buffer;
	}
}
