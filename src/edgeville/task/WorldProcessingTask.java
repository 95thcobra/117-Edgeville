package edgeville.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.ServerProcessor;
import edgeville.model.World;

import java.util.Collection;

/**
 * @author Simon on 3-2-2015.
 */
public class WorldProcessingTask implements Task {

	private static final Logger logger = LogManager.getLogger(ServerProcessor.class);

	@Override
	public void execute(World world) {
		world.cycle();
		//world.server().scriptExecutor().cycle();
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
