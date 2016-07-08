package edgeville.model.entity.player.interfaces;

import edgeville.model.entity.Player;
import edgeville.model.entity.player.EquipSlot;
import edgeville.model.entity.player.Loadout;
import edgeville.model.item.Item;
import edgeville.net.message.game.encoders.InterfaceText;
import edgeville.util.CombatFormula;
import edgeville.util.Varbit;

public class QuestTab {
	private Player player;

	public QuestTab(Player player) {
		this.player = player;
	}

	public void sendQuestTabTitle() {
		player.interfaces().sendInterfaceString(274, 10, "Players online: " + player.world().getPlayersOnline());
	}

	public void updateMaxHit(int maxHit) {
		player.interfaces().sendInterfaceString(274, 25, "Max hit: " + maxHit);
	}

	public void updateKills() {
		player.interfaces().sendInterfaceString(274, 22, "Kills: " + player.getKills());
	}

	public void updateDeaths() {
		player.interfaces().sendInterfaceString(274, 23, "Deaths: " + player.getDeaths());
	}

	public void prepareQuestTab() {
		final int questTabInterfaceId = 274;

		sendQuestTabTitle();
		player.interfaces().sendInterfaceString(questTabInterfaceId, 14, "Quick-gear"); // Second
																						// big
																						// string

		// Small strings start. COLORS <col=00AEDB>
		player.interfaces().sendInterfaceString(questTabInterfaceId, 15, "Melee gear");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 16, "Range gear");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 17, "Hybrid gear");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 18, "");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 19, "Save loadout");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 20, "Load loadout");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 21, "");
		updateKills();// 22
		updateDeaths();// 23
		player.interfaces().sendInterfaceString(questTabInterfaceId, 24, "");
		updateMaxHit(-1);// 25

		for (int child = 26; child < 143; child++) {
			player.write(new InterfaceText(questTabInterfaceId, child, ""));
		}
	}

	public void clickButton(int buttonId) {
		switch (buttonId) {

		// Melee
		case 15:
			player.spawnMelee();
			player.message("You have spawned some melee gear.");
			break;

		// Range
		case 16:
			player.spawnRanged();
			player.message("You have spawned some ranged gear.");
			break;

		// Hybrid
		case 17:
			player.spawnHybrid();
			player.message("You have spawned some hybrid gear.");
			break;

		// Save loadout
		case 19:
			player.getLoadout().save(player);
			player.message("Saved loadout.");
			break;

		// Load loadout
		case 20:
			player.getLoadout().load(player);
			player.message("Loaded loadout.");
			break;
		}
	}
}
