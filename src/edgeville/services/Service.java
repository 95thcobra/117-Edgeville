package edgeville.services;

import edgeville.GameServer;

/**
 * @author Simon on 4-3-2015.
 *
 * Basic service interface.
 */
public interface Service {

	/**
	 * Called immediately after construction, before even {@link edgeville.GameServer#start()} is called.
	 * Use this method only to set up references, and do connection and IO logic in the start method.
	 * @param server The server running this service instance.
	 * @param serviceConfig The configuration that was nested into the json config for the service.
	 */
	public void setup(GameServer server/*, Config serviceConfig*/);

	/**
	 * Attempt to start the service.
	 * @return <code>true</code> only if the service started without any issues and was previously not running.
	 *  If it failed to start properly, this returns <code>false</code> to indicate that this service cannot be used.
	 */
	public boolean start();

	/**
	 * Attempt to stop the active service. If the service was already stopped, this will return <code>false</code>.
	 * @return <code>true</code> only if the service stopped without issues and was not already stopped.
	 */
	public boolean stop();

	/**
	 * Checks if the service is still alive and available for use. This method is called periodically to ensure services
	 * are working as expected.
	 * @return <code>true</code> if the service is active and ready for work, <code>false</code> if not.
	 */
	public boolean isAlive();

}
