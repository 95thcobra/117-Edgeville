package edgeville.aquickaccess.actions;

import edgeville.combat.Combat;
import edgeville.combat.PlayerVersusAnyCombat;
import edgeville.combat.magic.AncientSpell;
import edgeville.combat.magic.RegularDamageSpell;
import edgeville.combat.magic.Runes;
import edgeville.combat.magic.Spell;
import edgeville.model.AttributeKey;
import edgeville.model.Entity;
import edgeville.model.Locations;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.EquipSlot;
import edgeville.model.entity.player.Skills;
import edgeville.model.entity.player.interfaces.inputdialog.NumberInputDialog;
import edgeville.model.entity.player.interfaces.inputdialog.StringInputDialog;
import edgeville.model.entity.player.skills.Prayer;
import edgeville.model.entity.player.skills.Prayers;
import edgeville.model.item.Item;
import edgeville.net.message.game.encoders.InvokeScript;
import edgeville.script.TimerKey;
import edgeville.util.CombatFormula;
import edgeville.util.CombatStyle;
import edgeville.util.SettingsBuilder;
import edgeville.util.TextUtil;
import edgeville.util.Varbit;
import edgeville.util.Varp;

/**
 * Created by Sky on 21-6-2016.
 */
public class ButtonClickAction {
	private Player player;
	private int interfaceId;
	private int buttonId;
	private int slot;
	private int option;

	public ButtonClickAction(Player player, int interfaceId, int buttonId, int slot, int option) {
		this.player = player;
		this.interfaceId = interfaceId;
		this.buttonId = buttonId;
		this.slot = slot;
		this.option = option;
	}

	// Add buttons here
	public void handleButtonClick() {
		switch (interfaceId) {

		// Bank: used for withdrawing.
		case 12:
			player.getBank().handleClick(buttonId, slot, option);
			break;

		// Inventory: used for banking & more
		case 15:
			if (option <= 6) {
				player.getBank().deposit(buttonId, slot, option);
			}

			// Advanced settings.
		case 60:
			advancedSettings();
			break;

		// quick prayers interface
		case 77:
			// done button
			if (buttonId == 5) {
				player.interfaces().setQuickPrayers(false);
			}
			break;
			
			// keybindings
		case 121:
			setupKeybindings();
			break;

		// XP Drops settings
		case 137:
			XPDropToggles();
			break;

		case 160:
			// XP Drops
			if (buttonId == 1) {
				setupXPDrops();
			}

			// quick prayers
			if (buttonId == 14) {
				quickPrayers();
			}

			// Toggle running
			if (buttonId == 22) {
				player.pathQueue().toggleRunning();
			}
			break;

		// Logout
		case 182:
			if (buttonId == 6) {
				player.logout();
			}
			break;

		// Spellbook
		case 218:
			handleSpellBook();
			break;

		// Run toggle, in settings
		case 261:
			
			if (buttonId == 57) {
				player.interfaces().sendMain(121);
				return;
			}
			if (buttonId == 66) {
				player.pathQueue().toggleRunning();
				return;
			}
			if (buttonId == 21) {
				player.interfaces().sendMain(60);
				return;
			}
			break;

		// activate prayer
		case 271:
			player.getPrayer().togglePrayer(buttonId);
			break;

		// Quest tab
		case 274:
			// handleQuestTab();
			player.getQuestTab().clickButton(buttonId);
			break;

		// Options: equipment stats, etc.
		case 387:
			if (buttonId == 11 && option == 1) {
				handleDfs();
				return;
			}
			handleOptionsTabs();
			break;

		// Combat style switching
		case 593:
			tabs();
			break;

		// Clanchat join chat
		case 589:
			handleClanChat();
			break;
		}
	}

	////////////////

	private void setupKeybindings() {
			//player.write(new InvokeScript(917, -1, -1));
			player.interfaces().sendMain(121);

			SettingsBuilder settingsBuilder = new SettingsBuilder();
			player.interfaces().setting(121, 21, 1, 14, settingsBuilder.option(0));
			player.interfaces().setting(121, 22, 1, 14, settingsBuilder.option(0));
			player.interfaces().setting(121, 23, 1, 14, settingsBuilder.option(0));
			/*player.interfaces().setting(121, 51, 1, 3, settingsBuilder.option(0));
			player.interfaces().setting(121, 52, 1, 4, settingsBuilder.option(0));
			player.interfaces().setting(121, 53, 1, 32, settingsBuilder.option(0));
			player.interfaces().setting(121, 54, 1, 32, settingsBuilder.option(0));
			player.interfaces().setting(121, 55, 1, 8, settingsBuilder.option(0));
			player.interfaces().setting(121, 56, 1, 2, settingsBuilder.option(0));
			player.interfaces().setting(121, 57, 1, 3, settingsBuilder.option(0));
			player.interfaces().setting(121, 16, 0, 24, settingsBuilder.option(0));*/
	}

	private void handleDfs() {
		Entity target = player.getTarget();
		if (target == null) {
			player.message("Find a target first!");
			return;
		}
		if (player.timers().has(TimerKey.DFS_COOLDOWN)) {
			player.message("Dragonfire shield hasn't recharged yet.");
			return;
		}
		int max = 25;
		int hit = player.world().random().nextInt((int) Math.round(max));

		player.graphic(1165, 92, 0);
		player.world().spawnProjectile(player.getTile(), target, 1166, 40, 33, 55, 12, 15, 50);
		target.graphic(1167, 92, 50);

		target.hit(player, hit, 3);

		player.timers().register(TimerKey.DFS_COOLDOWN, 50);
	}

	private void quickPrayers() {
		if (option == 1) {// save
			player.getPrayer().saveQuickPrayers();
		} else if (option == 0) {// activate
			player.getPrayer().toggleQuickPrayers();
		}
	}

	private void handleClanChat() {
		StringInputDialog inputDialog = new StringInputDialog(player) {
			@Override
			public void doAction(String value) {
				Player ccOwner = player.world().getPlayerByName(value).get();
				ccOwner.getClanChat().addPlayer(player);
			}
		};
		player.setInputDialog(inputDialog);
		return;
	}

	private void advancedSettings() {
		switch (buttonId) {
		case 12:
			boolean enabled = player.getVarps().getVarbit(Varbit.TRANSPARENT_CHAT_BOX) == 1;
			player.getVarps().setVarbit(Varbit.TRANSPARENT_CHAT_BOX, enabled ? 0 : 1);
			break;
		case 14:
			boolean clickThroughEnabled = player.getVarps().getVarbit(Varbit.CLICKTHROUGH_CHAT_BOX) == 1;
			player.getVarps().setVarbit(Varbit.CLICKTHROUGH_CHAT_BOX, clickThroughEnabled ? 0 : 1);
			break;
		}
	}

	private void handleOptionsTabs() {
		switch (buttonId) {

		// Equipment stats
		case 17:
			player.interfaces().sendMain(84);
			player.getEquipment().refreshEquipmentStatsInterface(player);
			break;

		// Unequip
		case 6:
			unequip(EquipSlot.HEAD);
			break;
		case 7:
			unequip(EquipSlot.CAPE);
			break;

		case 8:
			unequip(EquipSlot.AMULET);
			break;

		case 9:
			unequip(EquipSlot.WEAPON);
			break;

		case 10:
			unequip(EquipSlot.BODY);
			break;

		case 11:
			unequip(EquipSlot.SHIELD);
			break;

		case 12:
			unequip(EquipSlot.LEGS);
			break;

		case 13:
			unequip(EquipSlot.HANDS);
			break;

		case 14:
			unequip(EquipSlot.FEET);
			break;
		case 15:
			unequip(EquipSlot.RING);
			break;
		case 16:
			unequip(EquipSlot.AMMO);
			break;

		// Items on death
		case 21:
			player.interfaces().sendMain(102);
			break;
		}
	}

	// REMOVE EQUIPMENT ITEM
	private void unequip(int slot) {
		if (option != 0) {
			return;
		}
		if (player.locked()) {
			return;
		}
		Item item = player.getEquipment().get(slot);
		if (!player.getInventory().add(item).success()) {
			player.message("You don't have enough inventory space to do that.");
			return;
		}
		player.getEquipment().set(slot, null);
		player.getEquipment().refreshEquipmentStatsInterface(player);
	}

	private void tabs() {
		switch (buttonId) {

		// Attack styles
		case 3:
			player.getVarps().setVarp(Varp.ATTACK_STYLE, 0);
			break;
		case 7:
			player.getVarps().setVarp(Varp.ATTACK_STYLE, 1);
			break;
		case 11:
			player.getVarps().setVarp(Varp.ATTACK_STYLE, 2);
			break;
		case 15:
			player.getVarps().setVarp(Varp.ATTACK_STYLE, 3);
			break;

		// autocasting
		case 20:
		case 24:
			handleAutoCast();
			break;

		// Special attack
		case 30:
			if (isGmaulAttack()) {
				return;
			}
			if (specialSpecialAttack()) {
				// varps.setVarp(Varp.SPECIAL_ENABLED, isSpecialAttackEnabled()
				// ? 0 : 1);
				return;
			}
			player.toggleSpecialAttack();
			break;
		}
	}

	private void handleAutoCast() {
		if (player.isAutoCasting()) {
			player.disableAutocasting();
			player.message(TextUtil.colorString("You have canceled autocast!", TextUtil.Colors.RED));
			return;
		}
		if (player.getLastCastedSpell() == null) {
			player.message("Cast a spell you want to auto cast, then press this button!");
			return;
		}

		if (player.getVarps().getVarbit(Varbit.SPELLBOOK) == SpellBook.ANCIENTS && player.getLastCastedSpell() instanceof AncientSpell) {
			player.setAutoCastingSpell(player.getLastCastedSpell());
			player.setAutoCastingSpellChild(player.getLastSpellCastChild());
		} else if (player.getVarps().getVarbit(Varbit.SPELLBOOK) == SpellBook.REGULAR && player.getLastCastedSpell() instanceof RegularDamageSpell) {
			player.setAutoCastingSpell(player.getLastCastedSpell());
			player.setAutoCastingSpellChild(player.getLastSpellCastChild());
		} else {
			player.message("Your last casted spell was from a different spellbook!");
			return;
		}
		//player.setAutoCastingSpell(player.getLastCastedSpell());
		//player.setAutoCastingSpellChild(player.getLastSpellCastChild());

		player.getVarps().setVarbit(Varbit.AUTOCAST_SPELL, getAutoCastSpellId(player.getAutoCastingSpell()));
		player.getVarps().setVarbit(Varbit.AUTOCAST, buttonId == 20 ? 1 : 2);
		player.messageDebug("varbit autocast is now %d", player.getVarps().getVarbit(Varbit.AUTOCAST));
		String spellName = player.getLastCastedSpell().toString();
		player.message("<col=FF0000>You are now autocasting <col=0066FF>" + spellName + "<col=FF0000>!");
		player.setAutoCasting(true);
	}

	private int getAutoCastSpellId(Spell spell) {
		if (spell instanceof AncientSpell) {
			AncientSpell aSpell = (AncientSpell) spell;
			return 31 + aSpell.ordinal();
		} else {
			RegularDamageSpell rdSpell = (RegularDamageSpell) spell;
			return rdSpell.ordinal();
		}
	}

	private boolean specialSpecialAttack() {
		// player.varps().setVarp(Varp.SPECIAL_ENABLED, 0);
		Item weapon = player.getEquipment().get(EquipSlot.WEAPON);
		if (weapon == null) {
			return false;
		}
		switch (weapon.getId()) {
		// dragon battle axe
		case 1377:
			if (!player.drainSpecialEnergy(100)) {
				return true;
			}
			player.skills().alterSkill(Skills.ATTACK, 0.9);
			player.skills().alterSkill(Skills.DEFENCE, 0.9);
			player.skills().alterSkill(Skills.RANGED, 0.9);
			player.skills().alterSkill(Skills.MAGIC, 0.9);
			player.skills().alterSkill(Skills.STRENGTH, 1.1);
			player.graphic(246);
			break;
		// Not handled
		default:
			return false;
		}
		return true;
	}

	private boolean isGmaulAttack() {
		Item weapon = player.getEquipment().get(EquipSlot.WEAPON);
		int weaponId = weapon == null ? -1 : weapon.getId();

		if (weaponId != 4153) {
			return false;
		}

		Entity target = player.getAttribute(AttributeKey.TARGET);
		if (target != null) {
			PlayerVersusAnyCombat.handleGraniteMaul(player, target);
		}
		return true;
	}

	private void XPDropToggles() {
		switch (buttonId) {

		// Position
		case 50:
			player.getVarps().setVarbit(Varbit.XP_DROPS_POSITION, slot);
			break;

		// Size
		case 51:
			player.getVarps().setVarbit(Varbit.XP_DROPS_SIZE, slot);
			break;

		// Duration
		case 52:
			player.getVarps().setVarbit(Varbit.XP_DROPS_DURATION, slot);
			break;

		// Counter
		case 53:
			player.getVarps().setVarbit(Varbit.XP_DROPS_COUNTER, slot);
			break;

		// Progressbar
		case 54:
			player.getVarps().setVarbit(Varbit.XP_DROPS_PROGRESSBAR, slot);
			break;

		// Color
		case 55:
			player.getVarps().setVarbit(Varbit.XP_DROPS_COLOR, slot);
			break;

		// Group
		case 56:
			player.getVarps().setVarbit(Varbit.XP_DROPS_GROUP, slot);
			break;

		// Speed
		case 57:
			player.getVarps().setVarbit(Varbit.XP_DROPS_SPEED, slot);
			break;
		}
	}

	private void setupXPDrops() {
		// Toggle XP drops
		if (option == 0) {
			player.skills().toggleXPCounter();
			return;
		}

		// Setup XP drops
		if (option == 1) {
			player.write(new InvokeScript(917, -1, -1));
			player.interfaces().sendMain(137);

			SettingsBuilder settingsBuilder = new SettingsBuilder();
			player.interfaces().setting(137, 50, 1, 3, settingsBuilder.option(0));
			player.interfaces().setting(137, 51, 1, 3, settingsBuilder.option(0));
			player.interfaces().setting(137, 52, 1, 4, settingsBuilder.option(0));
			player.interfaces().setting(137, 53, 1, 32, settingsBuilder.option(0));
			player.interfaces().setting(137, 54, 1, 32, settingsBuilder.option(0));
			player.interfaces().setting(137, 55, 1, 8, settingsBuilder.option(0));
			player.interfaces().setting(137, 56, 1, 2, settingsBuilder.option(0));
			player.interfaces().setting(137, 57, 1, 3, settingsBuilder.option(0));
			player.interfaces().setting(137, 16, 0, 24, settingsBuilder.option(0));
			return;
		}
	}

	private void handleSpellBook() {
		if (option != 0) {
			return;
		}
		switch (buttonId) {

		// Edge teleport
		case 1:
		case 91:// ancient
		case 93:// lunar
			player.message("Teleporting to edgeville...");
			player.teleport(Locations.EDGEVILLE.getTile());
			break;

		// Varrock teleport
		case 16:
			player.message("Teleporting to varrock...");
			player.teleport(Locations.VARROCK.getTile());
			break;

		// veng
		case 132:
			castVeng();
			break;
		}
	}

	private void castVeng() {
		int levelReq = 94;
		if (levelReq > player.skills().level(Skills.MAGIC)) {
			player.message("You need a magic level of %d to cast %s.", levelReq, "Vengeance");
			return;
		}
		Item[] requiredRunes = new Item[] { new Item(Runes.ASTRAL_RUNE, 4), new Item(Runes.DEATH_RUNE, 2), new Item(Runes.EARTH_RUNE, 10) };
		for (Item item : requiredRunes) {
			if (!player.getInventory().contains(item.getId(), item.getAmount())) {
				player.message("You do not have the required runes to cast Vengeance!");
				return;
			}
		}
		if (player.isVengOn()) {
			player.message("Vengeance is already enabled!");
			return;
		}
		if (player.timers().has(TimerKey.VENGEANCE_COOLDOWN)) {
			player.message("Vengeance is on cooldown, wait %d seconds.", (int) Math.round(player.timers().timers().get(TimerKey.VENGEANCE_COOLDOWN).ticks() / 0.6));
			return;
		}
		player.graphic(726, 92, 0);
		player.animate(4410);
		player.timers().register(TimerKey.VENGEANCE_COOLDOWN, 50);
		player.setVengOn(true);
	}
}
