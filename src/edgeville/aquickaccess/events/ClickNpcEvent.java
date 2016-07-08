package edgeville.aquickaccess.events;

import edgeville.aquickaccess.actions.NpcClick1Action;
import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.model.entity.Npc;
import edgeville.model.entity.Player;

/**
 * Created by Sky on 21-6-2016.
 */
public class ClickNpcEvent extends Event {
    private Player player;
    private Npc npc;

    public ClickNpcEvent(Player player, Npc npc) {
        this.player = player;
        this.npc = npc;
    }

    @Override
    public void execute(EventContainer container) {
        if (player.getTile().distance(npc.getTile()) <= 3) {
            container.stop();
        }
    }

    @Override
    public void stop() {
        new NpcClick1Action().handleNpcClick(player, npc.id());
    }
}
