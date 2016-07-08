package edgeville.model.uid;

import edgeville.GameServer;
import edgeville.model.entity.Player;

/**
 * @author Simon on 4-3-2015.
 */
public abstract class UIDProvider {

	protected GameServer server;

	public UIDProvider(GameServer server) {
		this.server = server;
	}

	/**
	 * Try to acquire a new UID for a player. This method may return null if it fails to acquire one, in which case
	 * the server will fall back to any other method available.
	 * @param player The player which needs a new UID.
	 * @return A UID of whatever object type.
	 */
	public abstract Object acquire(Player player);

}
