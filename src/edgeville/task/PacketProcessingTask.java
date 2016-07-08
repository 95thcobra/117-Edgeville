package edgeville.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.model.World;

import java.util.Collection;

/**
 * @author Simon on 8/23/2014.
 *
 * Processes the scheduled actions for the players.
 */
public class PacketProcessingTask implements Task {

	private static final Logger logger = LogManager.getLogger(PacketProcessingTask.class);

	@Override
	public void execute(World world) {
		world.players().forEachShuffled(player -> {
			player.pendingActions().forEach(packet -> {
				try {
					packet.process(player);
				} catch (Exception e) {
					logger.error("Error processing message {} for player {}.", packet.getClass().getSimpleName(), player.name());
					logger.error("Caused by: ", e);
				}
			});

			// Remove actions
			player.pendingActions().clear();
		});
	}

	@Override
	public Collection<SubTask> createJobs(World world) {
		return null;
	}

	@Override
	public boolean isAsyncSafe() {
		return false;
	}

}
