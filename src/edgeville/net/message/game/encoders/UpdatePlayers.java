package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;

/**
 * @author Simon on 8/23/2014.
 */
public class UpdatePlayers implements Command {

	private RSBuffer buffer;

	public UpdatePlayers(RSBuffer payload) {
		buffer = payload;
	}

	@Override
	public RSBuffer encode(Player player) {
		return buffer;
	}
}
