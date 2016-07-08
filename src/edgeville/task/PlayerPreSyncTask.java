package edgeville.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.model.Area;
import edgeville.model.Tile;
import edgeville.model.World;
import edgeville.model.entity.PathQueue;
import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.DisplayMap;
import edgeville.net.message.game.encoders.SetItems;

import java.util.Collection;

/**
 * @author Simon on 8/23/2014.
 */
public class PlayerPreSyncTask implements Task {

	private static final Logger logger = LogManager.getLogger(PlayerPreSyncTask.class);

	@Override
	public void execute(World world) {
		world.players().forEach(this::preUpdate);
	}

	private void preUpdate(Player player) {
		// Sync containers, if dirty
		player.precycle();

		// Send map if necessary
		if (player.activeMap() == null) {
			player.write(new DisplayMap(player));
			player.world().syncMap(player, null);
		} else {
			Area prev = player.activeArea();
			int mapx = player.activeMap().x;
			int mapz = player.activeMap().z;
			int dx = player.getTile().x - mapx;
			int dz = player.getTile().z - mapz;

			if (dx <= 16 || dz <= 16 || dx >= 88 || dz >= 88) {
				player.write(new DisplayMap(player));
				player.world().syncMap(player, prev);
				player.channel().flush();
			}
		}

		// Process path
		if (!player.pathQueue().empty()) {
			PathQueue.Step walkStep = player.pathQueue().next();
			int walkDirection = PathQueue.calculateDirection(player.getTile().x, player.getTile().z, walkStep.x, walkStep.z);
			int runDirection = -1;
			player.setTile(new Tile(walkStep.x, walkStep.z, player.getTile().level));

			if ((walkStep.type == PathQueue.StepType.FORCED_RUN || player.pathQueue().running()) && !player.pathQueue().empty() && walkStep.type != PathQueue.StepType.FORCED_WALK) {
				PathQueue.Step runStep = player.pathQueue().next();
				runDirection = PathQueue.calculateDirection(player.getTile().x, player.getTile().z, runStep.x, runStep.z);
				player.setTile(new Tile(runStep.x, runStep.z, player.getTile().level));
			}

			player.sync().step(walkDirection, runDirection);
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
