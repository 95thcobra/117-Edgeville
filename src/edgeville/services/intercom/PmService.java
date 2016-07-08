package edgeville.services.intercom;

import edgeville.model.entity.Player;
import edgeville.services.Service;

/**
 * @author Simon on 12-3-2015.
 *
 * A service responsible for providing the private messaging actions as well
 * as online and offline statuses. Defaults to the single world approach.
 */
public interface PmService extends Service {

	public void onUserOnline(Player player);

	public void onUserOffline(Player player);

	public void privateMessageDispatched(Player from, String target, String message);

}
