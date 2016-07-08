package edgeville.aquickaccess.events;

import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.model.entity.Player;
import edgeville.util.Varp;

/**
 * Created by Sky on 27-6-2016.
 */
public class SpecialEnergyRegeneration extends Event {

    private Player player;
    private int tick = 0;

    public SpecialEnergyRegeneration(Player player) {
        this.player = player;
    }

    @Override
    public void execute(EventContainer container) {

        player.message("TICK:"+tick);

        // On tick 50 replenish energy.
        if (tick == 50) {
            int currentEnergy = player.getVarps().getVarp(Varp.SPECIAL_ENERGY);
            player.getVarps().setVarp(Varp.SPECIAL_ENERGY, Math.min(1000, currentEnergy + 100));
            tick = 0;
        }
        tick++;
    }
}
