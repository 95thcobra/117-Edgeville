package edgeville.model.entity.player.skills;

import java.util.ArrayList;
import java.util.List;

import edgeville.aquickaccess.actions.EquipmentRequirement.Skill;
import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Skills;
import edgeville.script.Timer;
import edgeville.util.Varbit;
import edgeville.util.Varp;

public class Prayer {
	private Player player;
	private List<Prayers> quickPrayers;

	public Prayer(Player player) {
		this.player = player;
		prepare();
		quickPrayers = new ArrayList<>();
	}

	public List<Prayers> getQuickPrayers() {
		return quickPrayers;
	}

	public void saveQuickPrayers() {
		quickPrayers.clear();

		for (Prayers prayer : Prayers.values()) {
			if (isPrayerOn(prayer)) {
				quickPrayers.add(prayer);
			}
		}
		player.message("Quick prayers saved!");
	}

	private void prepare() {
		player.getVarps().setVarp(Varp.PIETY_AND_CHILVALRY_GLOW, 16);
	}

	public void togglePrayer(int buttonId) {
		int varbit = 4100 + buttonId;
		boolean enabled = isPrayerOn(varbit);

		if (enabled) {
			deactivatePrayer(buttonId);
		} else {
			activatePrayer(buttonId);
		}
	}

	public void activatePrayer(Prayers prayer) {
		if (prayer == null) {
			return;
		}

		if (player.skills().level(Skills.PRAYER) <= 0) {
			player.message("You have run out of prayer!");
			return;
		}

		if (!hasRequirements(prayer)) {
			player.message("Your prayer level is too low, you need " + prayer.getPrayerLevelReq() + " to use this prayer.");
			deactivatePrayer(prayer);
			return;
		}

		prayer.deactivatePrayers(player);
		player.getVarps().setVarbit(prayer.getVarbit(), 1);
		if (prayer.getHeadIcon() > -1) {
			player.setPrayerHeadIcon(prayer.getHeadIcon());
		}

		player.world().getEventHandler().addEvent(player, prayer.getDrain(), false, new Event() {
			@Override
			public void execute(EventContainer container) {
				player.skills().alterSkill(Skills.PRAYER, -1, true);

				if (player.skills().level(Skills.PRAYER) <= 0) {
					deactivatePrayer(prayer);
					container.stop();
				}
				if (!isPrayerOn(prayer.getVarbit())) {
					container.stop();
				}
			}
		});
	}

	public void activatePrayer(int buttonId) {
		// TODO IF OVERHEADICON
		int varbit = 4100 + buttonId;

		Prayers prayer = Prayers.getPrayerForVarbit(varbit);
		activatePrayer(prayer);
	}

	public void deactivateAllPrayers() {
		for (Prayers prayer : Prayers.values()) {
			deactivatePrayer(prayer);
		}
	}

	private boolean hasRequirements(Prayers prayer) {
		// TODO chivalry and piety additional reqs.
		return player.skills().xpLevel(Skills.PRAYER) >= prayer.getPrayerLevelReq();
	}

	public void deactivatePrayer(Prayers prayer) {
		player.getVarps().setVarbit(prayer.getVarbit(), 0);
		if (prayer.getHeadIcon() > -1) {
			player.setPrayerHeadIcon(-1);
		}
	}

	public void deactivatePrayer(int buttonId) {
		int varbit = 4100 + buttonId;
		Prayers prayer = Prayers.getPrayerForVarbit(varbit);
		if (prayer == null) {
			return;
		}
		player.getVarps().setVarbit(varbit, 0);
		if (prayer.getHeadIcon() > -1) {
			player.setPrayerHeadIcon(-1);
		}
	}

	public boolean isPrayerOn(Prayers prayer) {
		return player.getVarps().getVarbit(prayer.getVarbit()) == 1;
	}

	public boolean isPrayerOn(int varbit) {
		return player.getVarps().getVarbit(varbit) == 1;
	}

	public void toggleQuickPrayers() {
		boolean enabled = player.getVarps().getVarbit(Varbit.PRAYER_ORB) == 1;
		player.getVarps().setVarbit(Varbit.PRAYER_ORB, enabled ? 0 : 1);

		deactivateAllPrayers();

		if (!enabled) {
			for (Prayers prayer : Prayers.values()) {
				if (quickPrayers.contains(prayer)) {
					activatePrayer(prayer);
				}
			}
		}

		player.message("Quickprayers %s!", enabled ? "deactivated" : "activated");
	}
}
