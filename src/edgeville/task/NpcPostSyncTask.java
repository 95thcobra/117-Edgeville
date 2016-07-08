package edgeville.task;

import java.util.Collection;

import edgeville.model.World;
import edgeville.model.entity.Npc;
import edgeville.model.entity.PathQueue;

/**
 * @author Simon on 8/10/2015.
 */
public class NpcPostSyncTask implements Task {

	@Override
	public void execute(World world) {
		world.npcs().forEach(this::postUpdate);
	}

	private void postUpdate(Npc npc) {
		npc.sync().clear();
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
