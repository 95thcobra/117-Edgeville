package edgeville.aquickaccess.events;

import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.model.Tile;
import edgeville.model.entity.Player;

/**
 * Created by Sky on 21-6-2016.
 */
public class TeleportEvent extends Event {

    private Player player;
    private Tile targetTile;
    private int tick = 0;

    public TeleportEvent(Player player, Tile targetTile) {
        this.player = player;
        this.targetTile = targetTile;
        player.lock();
    }

    @Override
    public void execute(EventContainer container) {
        switch (tick) {
            case 0:
            	player.sound(200);
                player.graphic(111, 92, 0);
                player.animate(714);
                break;
            case 3:
                player.move(targetTile);
                break;
            case 4:
                player.animate(715);
                player.sound(201);
                break;
            case 5:
                stop();
                container.stop();
                break;
        }
        tick++;
    }

    @Override
    public void stop() {
        player.unlock();
    }
}
