package edgeville.task;

import java.util.Collection;

import edgeville.model.World;
import edgeville.model.entity.Player;

/**
 * @author Simon on 5-3-2015.
 */
public class PlayerProcessingTask implements Task {

	@Override
	public void execute(World world) {
		world.players().forEachShuffled(Player::cycle);
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
