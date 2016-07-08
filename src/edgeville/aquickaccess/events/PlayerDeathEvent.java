package edgeville.aquickaccess.events;

import edgeville.Constants;
import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.model.Entity;
import edgeville.model.Locations;
import edgeville.model.entity.Npc;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Skills;
import edgeville.model.entity.player.skills.Prayers;
import edgeville.script.TimerKey;
import edgeville.util.ItemsOnDeath;
import edgeville.util.PkpSystem;
import edgeville.util.TextUtil;
import edgeville.util.TextUtil.Colors;
import edgeville.util.Varbit;
import edgeville.util.Varp;

/**
 * Created by Sky on 27-6-2016.
 */
public class PlayerDeathEvent extends Event {

	private Player player;
	private Entity killer;
	private int tick = 0;

	public PlayerDeathEvent(Player player) {
		this.player = player;
		this.killer = player.killer();
	}

	@Override
	public void execute(EventContainer container) {
		switch (tick) {
		case 0:
			player.lock();
			break;

		case 1:
			/*
			 * if (killer instanceof Player) {
			 * PkpSystem.handleDeath((Player)killer, player); }
			 */
			break;

		case 2:
			player.message("Oh dear, you are dead!");
			player.animate(2304);

			handleRetribution();
			break;

		case 5:
			if (killer instanceof Player) {
				killer.message(TextUtil.colorString("You wrecked " + player.getDisplayName() + ".", Colors.RED));
				((Player) killer).incrementKills();
				player.incrementDeaths();
				if (Constants.DROP_ITEMS_ON_DEATH) {
					ItemsOnDeath.dropItems((Player) killer, player);
				}
			}
			player.move(Locations.RESPAWN_LOCATION.getTile());
			break;

		case 6:
			player.skills().resetStats();
			player.timers().cancel(TimerKey.FROZEN);
			player.timers().cancel(TimerKey.STUNNED);
			player.getVarps().setVarp(Varp.SPECIAL_ENERGY, 1000);
			player.getVarps().setVarp(Varp.SPECIAL_ENABLED, 0);
			player.damagers().clear();
			player.face(null);

			player.getVarps().setVarbit(Varbit.PROTECT_FROM_MAGIC, 0);
			player.getVarps().setVarbit(Varbit.PROTECT_FROM_MELEE, 0);
			player.getVarps().setVarbit(Varbit.PROTECT_FROM_MISSILES, 0);

			player.getPrayer().deactivateAllPrayers();

			player.graphic(-1);
			player.animate(-1);
			player.setHp(100, 0);
			break;

		case 7:
			player.unlock();
			container.stop();
			break;
		}
		tick++;
	}

	private void handleRetribution() {
		if (player.getPrayer().isPrayerOn(Prayers.RETRIBUTION)) {
			Entity killer = player.killer();
			if (killer != null && killer.getTile().distance(player.getTile()) <= 1) {
				int hit = 25;
				if (killer instanceof Player) {
					hit = (int) Math.round(0.25 * ((Player) killer).skills().level(Skills.PRAYER));
				}
				killer.hit(player, hit); // retribution
				player.graphic(437);
			}
		}
	}
}
