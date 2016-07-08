package edgeville;

import io.netty.handler.traffic.TrafficCounter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.model.World;
import edgeville.task.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Simon on 8/23/2014.
 *
 * Represents the 'heart' of the game server, or the 'main thread' so to speak.
 * The ServerProcessor has a (linked) list of tasks it runs in sequence. If any of
 * the tasks in the sequence fails that tick, the other tasks will still run. The
 * failed task will simply be executed again the next cycle.
 */
public class ServerProcessor extends Thread {

	private static final Logger logger = LogManager.getLogger(ServerProcessor.class);

	/**
	 * A reference to the server to which this processor belongs.
	 */
	private GameServer server;

	/**
	 * A reference to our game world.
	 */
	private World world;

	/**
	 * A list of tasks which are run in order every 600ms 'tick'. This list is of type {@link java.util.LinkedList}.
	 */
	private List<Task> tasks = new LinkedList<>();

	/**
	 * As long as this boolean is true, we process every 600ms.
	 */
	private boolean running = true;

	/**
	 * Ticks until we print some information about the server.
	 */
	private int infotick = 100;

	/**
	 * The executor that will nicely execute all our async tasks.
	 */
	private ExecutorService taskExecutor;

	/**
	 * Small runnable jobs list to execute on logic thread (before all other tasks)
	 */
	private ConcurrentLinkedQueue<Runnable> logicJobs;

	/**
	 * Constructs a new ServerProcessor which automatically starts and proceeds
	 * to run the tasks it has registered.
	 *
	 * @param server The server whom we process for.
	 */
	public ServerProcessor(GameServer server) {
		this.server = server;
		this.world = server.world();
		taskExecutor = Executors.newWorkStealingPool();
		logicJobs = new ConcurrentLinkedQueue<>();

		// Add tasks
		tasks.add(new PacketProcessingTask());
		tasks.add(new PlayerProcessingTask());
		tasks.add(new NpcProcessingTask());
		tasks.add(new WorldProcessingTask());
		tasks.add(new PlayerPreSyncTask());
		tasks.add(new NpcPreSyncTask());
		tasks.add(new NpcViewportTask());
		tasks.add(new PlayerSyncTask());
		tasks.add(new NpcSyncTask());
		tasks.add(new PlayerPostSyncTask());
		tasks.add(new NpcPostSyncTask());

		tasks.add(new NetworkFlushTask());

		start();
	}

	public void submitLogic(Runnable r) {
		logicJobs.add(r);
	}

	@Override
	public void run() {
		while (running) {
			process();
		}
	}

	private void process() {
		long start = System.currentTimeMillis();

		// Execute logic jobs
		logicJobs.forEach((runnable) -> {
			try {
				runnable.run();
			} catch (Exception e) {
				logger.error("Error executing logic job!", e);
			}
		});
		logicJobs.clear();

		for (Task t : tasks) {
			try {
				if (t.isAsyncSafe()) { // Is this job distributable across multiple workers?
					Collection<SubTask> jobs = t.createJobs(world);
					List<Future<Object>> futures = taskExecutor.invokeAll(jobs);

					while (futures.stream().anyMatch(f -> !f.isDone())) {
						Thread.sleep(1); // Sleep a small millisecond to go easy on the cpu.
					}
				} else { // Simple non-thread safe job that must execute all by itself.
					t.execute(world);
				}
			} catch (Throwable throwable) {
				logger.error("An exception occurred when executing " + t.getClass().getSimpleName() + ".", throwable);
			}
		}

		long delay = 600 - (System.currentTimeMillis() - start);

		if (infotick-- == 0) {
			infotick = 1000;

			long totalMem = Runtime.getRuntime().totalMemory();
			long freeMem = Runtime.getRuntime().freeMemory();
			long maxMem = Runtime.getRuntime().maxMemory();
			TrafficCounter traffic = server.initializer().trafficStats();
			logger.info("Cycle time: {}ms, players: {}. Memory usage: {}MB/{}MB. Reserved: {}MB. Rx: {}KB/s, Tx: {}KB/s.",
					System.currentTimeMillis() - start, world.players().size(), (totalMem - freeMem) / 1024 / 1024,
					totalMem / 1024 / 1024, maxMem / 1024 / 1024, traffic.lastReadThroughput() / 1024, traffic.lastWriteThroughput() / 1024);
		}

		if (delay < 0) {
			logger.warn("Server cannot keep up! Cycle overdue: {}ms.", -delay);
		} else {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Stops the processor in a graceful way, interrupting nothing but only avoiding execution of a next 600ms-cycle.
	 */
	public void terminate() {
		running = false;
	}

}
