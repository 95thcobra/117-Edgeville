package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;

/**
 * @author Simon on 8/23/2014.
 */
public class PlayMusic implements Command {

	private int track;

	public PlayMusic(int track) {
		this.track = track;
	}

	@Override public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(3));
		buffer.packet(231);

		buffer.writeShort(track);

		return buffer;
	}
}
