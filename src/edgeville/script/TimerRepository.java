package edgeville.script;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon on 8/12/2015.
 */
public class TimerRepository {

	private Map<TimerKey, Timer> timers = new EnumMap<>(TimerKey.class);

	public boolean has(TimerKey key) {
		Timer timer = timers.get(key);
		return timer != null && timer.ticks() > 0;
	}

	public void register(Timer timer) {
		timers.put(timer.key(), timer);
	}

	public void register(TimerKey key, int ticks) {
		timers.put(key, new Timer(key, ticks));
	}

	public void extendOrRegister(TimerKey key, int ticks) {
		timers.compute(key, (k, t) -> t == null || t.ticks() < ticks ? new Timer(key, ticks) : t);
	}

	public void cancel(TimerKey name) {
		timers.remove(name);
	}

	public void cycle() {
		timers.values().forEach(Timer::tick);
	}

	public Map<TimerKey, Timer> timers() {
		return timers;
	}

}
