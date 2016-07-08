package edgeville.aquickaccess.events;

import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.model.Entity;
import edgeville.model.entity.Npc;
import edgeville.model.entity.Player;

/**
 * Created by Sky on 27-6-2016.
 */
public class NpcDeathEvent extends Event {

	private Npc npc;
	private boolean respawn;
	private int tick;
	Entity killer;

	public NpcDeathEvent(Npc npc, boolean respawn) {
		this.npc = npc;
		this.respawn = respawn;
		tick = 0;
		killer = npc.killer();
	}

	@Override
	public void execute(EventContainer container) {
		switch (tick) {
		case 0:
			npc.lock();
			break;

		case 1:
			if (killer != null && killer instanceof Player) {
				killer.message("You have killed " + npc.def().name);
			}
			break;

		case 2:
			npc.animate(getNpcDeathAnim());
			if (killer != null && killer instanceof Player) {
				((Player) killer).sound(getNpcDeathSound());
			}
			break;

		case 5:
			npc.world().unregisterNpc(npc);
			if (!respawn) {
				container.stop();
			}
			break;

		case 30:
			npc.setHp(100);
			npc.unlock();
			npc.world().registerNpc(npc);
			container.stop();
			break;
		}
		tick++;
	}
	
	private int getNpcDeathAnim() {
		int animId = 2304;
		switch(npc.id()) {
		case 2005:
			animId = 92;
			break;
		}
		return animId;
	}
	
	private int getNpcDeathSound() {
		int soundId = -1;
		switch(npc.id()) {
		case 2005:
			soundId = 403;
			break;
		}
		return soundId;
	}
}
