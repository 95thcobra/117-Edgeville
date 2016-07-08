package edgeville.task;

import java.util.Collection;

import edgeville.model.World;
import edgeville.model.entity.Player;

/**
 * @author Simon on 8/23/2014.
 */
public class PlayerPostSyncTask implements Task {

	@Override
	public void execute(World world) {
		world.players().forEach(this::postUpdate);
	}

	private void postUpdate(Player player) {
		player.sync().clear();
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
