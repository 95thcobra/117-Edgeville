package edgeville.task;

import java.util.concurrent.Callable;

import edgeville.model.World;

/**
 * @author Simon on 8/2/2015.
 */
public abstract class SubTask implements Callable<Object> {

	protected World world;

	public SubTask(World world) {
		this.world = world;
	}

	@Override
	public Object call() throws Exception {
		execute();
		return null;
	}

	public abstract void execute();

}
