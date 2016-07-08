package edgeville.task;

import java.util.Collection;

import edgeville.model.Tile;
import edgeville.model.World;
import edgeville.model.entity.Npc;
import edgeville.model.entity.PathQueue;
import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.DisplayMap;

/**
 * @author Simon on 8/10/2015.
 */
public class NpcPreSyncTask implements Task {

	@Override
	public void execute(World world) {
		world.npcs().forEach(this::preUpdate);
	}

	private void preUpdate(Npc npc) {
		// Process path
		if (!npc.pathQueue().empty()) {
			PathQueue.Step walkStep = npc.pathQueue().next();
			int walkDirection = PathQueue.calculateDirection(npc.getTile().x, npc.getTile().z, walkStep.x, walkStep.z);
			int runDirection = -1;
			npc.setTile(new Tile(walkStep.x, walkStep.z, npc.getTile().level));

			if ((walkStep.type == PathQueue.StepType.FORCED_RUN || npc.pathQueue().running()) && !npc.pathQueue().empty() && walkStep.type != PathQueue.StepType.FORCED_WALK) {
				PathQueue.Step runStep = npc.pathQueue().next();
				runDirection = PathQueue.calculateDirection(npc.getTile().x, npc.getTile().z, runStep.x, runStep.z);
				npc.setTile(new Tile(walkStep.x, walkStep.z, npc.getTile().level));
			}

			npc.sync().step(walkDirection, runDirection);
		}
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
