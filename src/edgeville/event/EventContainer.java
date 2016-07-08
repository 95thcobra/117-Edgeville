package edgeville.event;

import edgeville.model.Entity;
import edgeville.model.entity.Player;

/**
 * Created by Sky on 3-3-2016.
 */
public class EventContainer {

    private Entity entity;
    private boolean isRunning;
    private int ticks;
    private Event event;
    private int cyclesPassed;
    private boolean cancellable;

    public EventContainer(Entity entity, int ticks, boolean cancellable, Event event) {
        this.entity = entity;
        this.event = event;
        this.isRunning = true;
        this.cyclesPassed = 0;
        this.ticks = ticks;
        this.cancellable = cancellable;
    }

    public void execute() {
        event.execute(this);
    }

    public void stop() {
        isRunning = false;
        event.stop();
    }

    public boolean needsExecution() {
       // System.out.println("CYCLES PASSED: " + cyclesPassed);
       // System.out.println("TICKL: " + ticks);
        if (cyclesPassed >= ticks) {
            this.cyclesPassed = 0;
            return true;
        }
        cyclesPassed++;
        return false;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isCancellable() {
        return cancellable;
    }
}
