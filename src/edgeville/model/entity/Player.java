package edgeville.model.entity;

import com.google.common.base.MoreObjects;

import edgeville.Constants;
import edgeville.Panel;
import edgeville.aquickaccess.events.PlayerDeathEvent;
import edgeville.aquickaccess.events.TeleportEvent;
import edgeville.combat.CombatUtil;
import edgeville.combat.magic.AncientSpell;
import edgeville.combat.magic.Spell;
import edgeville.crypto.IsaacRand;
import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.model.*;
import edgeville.model.clanchat.ClanChat;
import edgeville.model.entity.player.*;
import edgeville.model.entity.player.interfaces.InputDialog;
import edgeville.model.entity.player.interfaces.QuestTab;
import edgeville.model.entity.player.skills.Prayer;
import edgeville.model.item.Item;
import edgeville.model.item.ItemContainer;
import edgeville.model.item.ItemContainer.Type;
import edgeville.net.future.ClosingChannelFuture;
import edgeville.net.message.game.encoders.*;
import edgeville.script.Timer;
import edgeville.script.TimerKey;
import edgeville.services.serializers.PlayerSerializer;
import edgeville.util.CombatStyle;
import edgeville.util.StaffData;
import edgeville.util.TextUtil;
import edgeville.util.Varbit;
import edgeville.util.Varp;
import edgeville.util.TextUtil.Colors;
import io.netty.channel.Channel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Simon.
 */
public class Player extends Entity {

	/**
	 * A unique ID to identify a player, even after he or she has disconnected.
	 */
	private Object id;

	/**
	 * The name that the player had used to log in with (not always the display
	 * name!)
	 */
	private String username;

	private String password;

	/**
	 * The name of the player, actually seen in-game.
	 */
	private String displayName;

	/**
	 * The player's Netty connection channel
	 */
	private Channel channel;

	/**
	 * The privilege level of this player.
	 */
	private Privilege privilege;

	/**
	 * Our achieved skill levels
	 */
	private Skills skills;

	private boolean debug;

	private CombatUtil combatUtil;

	private boolean vengOn;

	public boolean isVengOn() {
		return vengOn;
	}

	public void setVengOn(boolean vengOn) {
		this.vengOn = vengOn;
	}

	public CombatUtil getCombatUtil() {
		return combatUtil;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	// for now init it right away, TODO remove
	private ClanChat clanChat;

	public ClanChat getClanChat() {
		return clanChat;
	}

	public void setClanChat(ClanChat clanChat) {
		this.clanChat = clanChat;
	}

	/**
	 * Our looks (clothes, colours, gender)
	 */
	private Looks looks;

	private Interfaces interfaces;

	private QuestTab questTab;

	public QuestTab getQuestTab() {
		return questTab;
	}

	private int autoCastingSpellChild;

	private int lastSpellCastChild;

	public int getLastSpellCastChild() {
		return lastSpellCastChild;
	}

	public void setLastSpellCastChild(int lastSpellCastChild) {
		this.lastSpellCastChild = lastSpellCastChild;
	}

	private Spell lastCastedSpell;

	private Spell autoCastingSpell;

	public Spell getAutoCastingSpell() {
		return autoCastingSpell;
	}

	public void setAutoCastingSpell(Spell autoCastingSpell) {
		this.autoCastingSpell = autoCastingSpell;
	}

	public Spell getLastCastedSpell() {
		return lastCastedSpell;
	}

	public void setLastCastedSpell(Spell lastCastedSpell) {
		this.lastCastedSpell = lastCastedSpell;
	}

	private int kills;
	private int deaths;

	public int getKills() {
		return kills;
	}

	public void setKills(int kills) {
		this.kills = kills;
		this.getQuestTab().updateKills();
	}

	public void incrementKills() {
		this.kills += 1;
		this.getQuestTab().updateKills();
	}

	public void incrementDeaths() {
		this.deaths += 1;
		this.getQuestTab().updateDeaths();
	}

	public int getDeaths() {
		return deaths;
	}

	public void setDeaths(int deaths) {
		this.deaths = deaths;
		this.getQuestTab().updateDeaths();
	}

	/**
	 * The map which was recently sent to show
	 */
	private Tile activeMap;

	/**
	 * The ISAAC Random Generator for incoming packets.
	 */
	private IsaacRand inrand;

	private Prayer prayer;

	public Prayer getPrayer() {
		return prayer;
	}

	/**
	 * The ISAAC Random Generator for outgoing packets.
	 */
	private IsaacRand outrand;

	private int skullHeadIcon = -1;
	private int prayerHeadIcon = -1;

	public int getSkullHeadIcon() {
		return skullHeadIcon;
	}

	public void setSkullHeadIcon(int skullHeadIcon) {
		this.skullHeadIcon = skullHeadIcon;
		looks().update();
	}

	public int getPrayerHeadIcon() {
		return prayerHeadIcon;
	}

	public void setPrayerHeadIcon(int prayerHeadIcon) {
		this.prayerHeadIcon = prayerHeadIcon;
		looks().update();
	}

	private Loadout loadout;

	public void setLoadout(Loadout loadout) {
		this.loadout = loadout;
	}

	public Loadout getLoadout() {
		return loadout;
	}

	/**
	 * A list of pending actions which are decoded at the next game cycle.
	 */
	private ConcurrentLinkedQueue<Action> pendingActions = new ConcurrentLinkedQueue<Action>();

	private ItemContainer inventory;
	private ItemContainer equipment;

	private Bank bank;

	public Bank getBank() {
		return bank;
	}

	public void setBank(Bank bank) {
		this.bank = bank;
	}

	// private void lastButtonClicked

	private Varps varps;
	// private InputHelper inputHelper;

	private InputDialog lastInputDialog;

	private int dialogueAction = -1;

	public int getDialogueAction() {
		return dialogueAction;
	}

	private boolean isXPCounterEnabled = true;

	public boolean isXPCounterEnabled() {
		return isXPCounterEnabled;
	}

	public void setXPCounterEnabled(boolean XPCounterEnabled) {
		isXPCounterEnabled = XPCounterEnabled;
	}

	public void setDialogueAction(int dialogueAction) {
		this.dialogueAction = dialogueAction;
	}

	/**
	 * The ID of the last applied migration.
	 */
	private int migration;

	private boolean isAutoCasting;

	private boolean receivedStarter;

	public boolean initialized;

	public boolean isAutoCasting() {
		return isAutoCasting;
	}

	public void setAutoCasting(boolean isAutoCasting) {
		this.isAutoCasting = isAutoCasting;
	}

	public Player(Channel channel, String username, String password, World world, Tile tile, IsaacRand inrand,
			IsaacRand outrand) {
		super(world, tile);

		this.channel = channel;
		this.inrand = inrand;
		this.outrand = outrand;
		this.username = this.displayName = username;
		this.password = password;
		// this.privilege = privilege;

		this.sync = new PlayerSyncInfo(this);
		this.skills = new Skills(this);
		this.looks = new Looks(this);
		//this.interfaces = new Interfaces(this);
		//this.inventory = new ItemContainer(world, 28, ItemContainer.Type.REGULAR);
		this.equipment = new ItemContainer(world, 14, ItemContainer.Type.REGULAR);
		// this.bank = new ItemContainer(world, 800,
		// ItemContainer.Type.FULL_STACKING);
		this.varps = new Varps(this);
		// this.lastInputDialog = new InputDialog(this);

		prayer = new Prayer(this);
		loadout = new Loadout();
		questTab = new QuestTab(this);

		looks().update();

		/////// sj
		debug = false;
		resetSpecialEnergy();
		// bank = new Bank(this);
		bank = new Bank(this);
		combatUtil = new CombatUtil(this);
		getVarps().setVarbit(Varbit.XP_DROPS_ORB, 1);
		getVarps().setVarbit(Varbit.XP_DROPS_COUNTER, 31);

		// remove this TODO
		clanChat = new ClanChat(this, username + "'s clanchat", new ArrayList<Player>());

		getVarps().presave();
	}

	public void giveStarterPack() {
		spawnMelee();
		presetBank();
		message(TextUtil.colorString("Check the quest tab for spawns!", TextUtil.Colors.RED));
		setReceivedStarter(true);
	}

	private void onLogin() {
		getVarps().setVarbit(Varbit.PRAYER_ORB, 0);
		if (!isAutoCasting) {
			getVarps().setVarbit(Varbit.AUTOCAST, 0);
			getVarps().setVarbit(Varbit.AUTOCAST_SPELL, 0);
		}
	}

	private void presetBank() {
		bank.getBankItems().add(new Item(4708, 1000));
		bank.getBankItems().add(new Item(4712, 1001));
		bank.getBankItems().add(new Item(6920, 1001));
		bank.getBankItems().add(new Item(4714, 1001));
		bank.getBankItems().add(new Item(6585, 1001));
		bank.getBankItems().add(new Item(7462, 41));
		bank.getBankItems().add(new Item(6914, 1000));
		bank.getBankItems().add(new Item(6889, 1001));
		bank.getBankItems().add(new Item(2414, 20));
		bank.getBankItems().add(new Item(4736, 1002));
		bank.getBankItems().add(new Item(12006, 22));
		bank.getBankItems().add(new Item(6570, 22));
		bank.getBankItems().add(new Item(12954, 22));
		bank.getBankItems().add(new Item(11840, 1002));
		bank.getBankItems().add(new Item(11832, 20));
		bank.getBankItems().add(new Item(11834, 1000));
		bank.getBankItems().add(new Item(11802, 20));
		bank.getBankItems().add(new Item(10828, 21));
		bank.getBankItems().add(new Item(6737, 1000));
		bank.getBankItems().add(new Item(5698, 1003));
		bank.getBankItems().add(new Item(4753, 1000));
		bank.getBankItems().add(new Item(10370, 1000));
		bank.getBankItems().add(new Item(10372, 1000));
		bank.getBankItems().add(new Item(4759, 1000));
		bank.getBankItems().add(new Item(10696, 20));
		bank.getBankItems().add(new Item(6733, 20));
		bank.getBankItems().add(new Item(11785, 20));
		bank.getBankItems().add(new Item(9244, 1000000));
		bank.getBankItems().add(new Item(11284, 20));
		bank.getBankItems().add(new Item(10499, 20));
		bank.getBankItems().add(new Item(4716, 1000));
		bank.getBankItems().add(new Item(4720, 1000));
		bank.getBankItems().add(new Item(4722, 1002));
		bank.getBankItems().add(new Item(4718, 1000));
		bank.getBankItems().add(new Item(4153, 1001));
		bank.getBankItems().add(new Item(4675, 1001));
		bank.getBankItems().add(new Item(6918, 1000));
		bank.getBankItems().add(new Item(6916, 1000));
		bank.getBankItems().add(new Item(6924, 1000));
		bank.getBankItems().add(new Item(10551, 22));
		bank.getBankItems().add(new Item(2617, 1000));
		bank.getBankItems().add(new Item(8850, 20));
		bank.getBankItems().add(new Item(3105, 1000));
		bank.getBankItems().add(new Item(2503, 1000));
		bank.getBankItems().add(new Item(6685, 1000));
		bank.getBankItems().add(new Item(3024, 1000));
		bank.getBankItems().add(new Item(2440, 1000));
		bank.getBankItems().add(new Item(2436, 1000));
		bank.getBankItems().add(new Item(397, 1028));
		bank.getBankItems().add(new Item(555, 1020000));
		bank.getBankItems().add(new Item(565, 1020000));
		bank.getBankItems().add(new Item(560, 1020000));
		bank.getBankItems().add(new Item(9075, 1000000));
		bank.getBankItems().add(new Item(557, 1000000));
		bank.getBankItems().add(new Item(385, 1000));
		bank.getBankItems().add(new Item(2448, 1000));
		bank.getBankItems().add(new Item(157, 1));
		bank.getBankItems().add(new Item(163, 1));
		bank.getBankItems().add(new Item(145, 1));
		bank.getBankItems().add(new Item(2412, 1));
		bank.getBankItems().add(new Item(11773, 1));
	}

	public void resetSpecialEnergy() {
		varps.setVarp(Varp.SPECIAL_ENERGY, 1000);
	}

	public void toggleSpecialAttack() {
		varps.setVarp(Varp.SPECIAL_ENABLED, isSpecialAttackEnabled() ? 0 : 1);
	}

	public void turnOffSpecialAttack() {
		varps.setVarp(Varp.SPECIAL_ENABLED, 0);
	}

	public boolean isSpecialAttackEnabled() {
		return varps.getVarp(Varp.SPECIAL_ENABLED) == 1;
	}

	public int getSpecialEnergyAmount() {
		return varps.getVarp(Varp.SPECIAL_ENERGY);
	}

	public void setSpecialEnergyAmount(int amount) {
		varps.setVarp(Varp.SPECIAL_ENERGY, Math.min(1000, amount));
	}

	/**
	 * No-args constructor solely for Hibernate.
	 */
	public Player() {
		super(null, null);
	}

	/**
	 * Sends everything required to make the user see the game.
	 */
	public void initiate() {
		skills.update();

		// Send simple player options

		write(new SetPlayerOption(2, false, "Follow"));
		write(new SetPlayerOption(3, false, "Trade with"));

		// Trigger a scripting event
		// world.server().scriptRepository().triggerLogin(this);

		// Execute groovy plugin
		// world.getPluginHandler().execute(this, LoginPlugin.class, new
		// LoginPlugin());

		varps.sync(1055);

		updatePrivileges();

		looks.update();

		// By default debug is on for admins
		// putattrib(AttributeKey.DEBUG, false/*privilege == Privilege.ADMIN*/);

		// Sync varps
		varps.syncNonzero();

		/////////// sj

		// Welcome
		message("Welcome to %s.", TextUtil.colorString(Constants.SERVER_NAME, Colors.BLUE));
		messageFilterable("The server is in development stage.");

		// Start energy regenerate timer
		timers().register(TimerKey.SPECIAL_ENERGY_RECHARGE, 50);

		// Replenish stats timer
		timers().register(TimerKey.STAT_REPLENISH, 100);

		// quest tab
		questTab.prepareQuestTab();

		if (!receivedStarter)
			giveStarterPack();

		onLogin();
		// new Panel(this);
	}

	public void event(Event event) {
		event(event, 1);
	}

	public void event(Event event, int ticks) {
		world.getEventHandler().addEvent(this, ticks, event);
	}

	public void updatePrivileges() {
		for (StaffData staff : StaffData.values()) {
			if (staff == null)
				continue;
			if (username.equalsIgnoreCase(staff.name()))
				privilege(staff.getPrivilege());
		}
	}

	public String name() {
		return WordUtils.capitalize(displayName);
	}

	public void displayName(String n) {
		displayName = n;
	}

	public String getUsername() {
		return username;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getPassword() {
		return password;
	}

	public void message(String format, Object... params) {
		write(new AddMessage(params.length > 0 ? String.format(format, (Object[]) params) : format));
	}

	@Override
	public void stopActions(boolean cancelMoving) {
		super.stopActions(cancelMoving);

		// Remove banking interface when banking
		if (interfaces.visible(12)) {
			interfaces().closeById(15);
		}

		// Reset main interface
		if (interfaces.visible(interfaces.activeRoot(), interfaces.mainComponent())) {
			interfaces.close(interfaces.activeRoot(), interfaces.mainComponent());
		}

		// Reset chatbox interface
		if (interfaces.visible(162, 546)) {
			interfaces.close(162, 546);
		}
	}

	public void filterableMessage(String format, Object... params) {
		write(new AddMessage(params.length > 0 ? String.format(format, (Object[]) params) : format,
				AddMessage.Type.GAME_FILTER));
	}

	public void id(Object id) {
		this.id = id;
	}

	public Object id() {
		return id; // Temporary!
	}

	public ConcurrentLinkedQueue<Action> pendingActions() {
		return pendingActions;
	}

	public Looks looks() {
		return looks;
	}

	public Channel channel() {
		return channel;
	}

	public Skills skills() {
		return skills;
	}

	public Tile activeMap() {
		return activeMap;
	}

	public Area activeArea() {
		return new Area(activeMap.x, activeMap.z, activeMap.x + 104, activeMap.z + 104);
	}

	public void activeMap(Tile t) {
		activeMap = t;
	}

	public boolean seesChunk(int x, int z) {
		return activeArea().contains(new Tile(x, z));
	}

	public IsaacRand inrand() {
		return inrand;
	}

	public IsaacRand outrand() {
		return outrand;
	}

	public Privilege getPrivilege() {
		// return privilege;
		return Privilege.ADMIN;
	}

	public void privilege(Privilege p) {
		privilege = p;
	}

	public Interfaces interfaces() {
		return interfaces;
	}

	public ItemContainer getInventory() {
		return inventory;
	}

	public void setInventory(ItemContainer inventory) {
		this.inventory = inventory;
	}

	public ItemContainer getEquipment() {
		return equipment;
	}

	public void setEquipment(ItemContainer equipment) {
		this.equipment = equipment;
	}

	/*
	 * public ItemContainer bank() { return bank; }
	 */

	public Varps getVarps() {
		return varps;
	}

	public void migration(int m) {
		migration = m;
	}

	public int migration() {
		return migration;
	}

	public InputDialog getLastInputDialog() {
		return lastInputDialog;
	}

	public void setInputDialog(InputDialog inputDialog) {
		this.lastInputDialog = inputDialog;
	}

	@Override
	public int hp() {
		return skills.level(Skills.HITPOINTS);
	}

	@Override
	public int maxHp() {
		return skills.xpLevel(Skills.HITPOINTS);
	}

	@Override
	public void setHp(int hp, int exceed) {
		skills.levels()[Skills.HITPOINTS] = Math.max(0, Math.min(maxHp() + exceed, hp));
		skills.update(Skills.HITPOINTS);
	}

	@Override
	public PlayerSyncInfo sync() {
		return (PlayerSyncInfo) sync;
	}

	public void sound(int id) {
		write(new PlaySound(id, 0));
	}

	public void sound(int id, int delay) {
		write(new PlaySound(id, delay));
	}

	public void sound(int id, int delay, int times) {
		write(new PlaySound(id, delay, times));
	}

	public void invokeScript(int id, Object... args) {
		write(new InvokeScript(id, args));
	}

	public void forceMove(ForceMovement move) {
		Tile t = pathQueue.peekLast() == null ? tile : pathQueue.peekLast().toTile();
		int bx = t.x - activeMap.x;
		int bz = t.z - activeMap.z;
		move.dx1 += bx;
		move.dx2 += bx;
		move.dz1 += bz;
		move.dz2 += bz;
		sync().forceMove(move);
	}

	/**
	 * Unregisters this player from the world it's in.
	 */
	public void unregister() {
		world.unregisterPlayer(this);
		world.server().service(PlayerSerializer.class, true).get().savePlayer(this);
	}

	/**
	 * Dispatches a logout message, and hooks a closing future to that. Once
	 * it's flushed, the channel is closed. The player is also immediately
	 * removed from the player list.
	 */
	public void logout() {

		prayer.deactivateAllPrayers();

		// If we're logged in and the channel is active, begin with sending a
		// logout message and closing the channel.
		// We use writeAndFlush here because otherwise the message won't be
		// flushed cos of the next unregister() call.
		if (channel.isActive()) {
			channel.writeAndFlush(new Logout()).addListener(new ClosingChannelFuture());
		}

		// Then nicely unregister the player from the game.
		unregister();
	}

	public void setMaster() {
		if (inWilderness()) {
			message("You cannot do this while in the wilderness.");
			return;
		}
		if (inCombat()) {
			message("You cannot do this in combat!.");
			return;
		}
		for (int i = 0; i < Skills.SKILL_COUNT; i++) {
			skills.setXp(i, 13034431);
			skills.resetStats();
		}
	}

	@Override
	public void cycle() {
		super.cycle();
/*
		// Are we requested to be logged out?
		if ((boolean) attribute(AttributeKey.LOGOUT, false)) {
			putAttribute(AttributeKey.LOGOUT, false);

			// Attempt to log us out. In the future, we'd want to do combat
			// checking and such here.
			logout();
			return;
		}

		// Fire timers
		for (Iterator<Map.Entry<TimerKey, Timer>> it = timers.timers().entrySet().iterator(); it.hasNext();) {
			Map.Entry<TimerKey, Timer> entry = it.next();
			if (entry.getValue().ticks() < 1) {
				TimerKey key = entry.getKey();
				it.remove();

				switch (key) {
				case SPECIAL_ENERGY_RECHARGE:
					int currentEnergy = getVarps().getVarp(Varp.SPECIAL_ENERGY);
					getVarps().setVarp(Varp.SPECIAL_ENERGY, Math.min(1000, currentEnergy + 100));
					timers.register(TimerKey.SPECIAL_ENERGY_RECHARGE, 50);
					break;
				case SKULL:
					if (getSkullHeadIcon() == Skulls.WHITE_SKUL.getSkullId()) {
						setSkullHeadIcon(-1);
					}
					break;
				case STAT_REPLENISH:
					skills.replenishStats();
					timers.register(TimerKey.STAT_REPLENISH, 100);
					break;
				}
				// world.server().scriptRepository().triggerTimer(this, key);
			}
		}

		interfaces.showSkull(!inSafeArea());

		questTab.sendQuestTabTitle();

		// Region enter and leave triggers
		int lastregion = attribute(AttributeKey.LAST_REGION, -1);

		putAttribute(AttributeKey.LAST_REGION, tile.region());

		// Show attack option when player is in wilderness.
		handlePlayerOptions();
		
		*/
	}

	private void handlePlayerOptions() {
		if (Constants.ALL_PVP) {
			if (!inSafeArea() || canBeAttackInSafeArea()) {
				write(new SetPlayerOption(1, true, "Attack"));
			} else {
				write(new SetPlayerOption(1, true, "Null"));
			}
			return;
		}

		if (inWilderness()) {
			write(new SetPlayerOption(1, true, "Attack"));
		} else {
			write(new SetPlayerOption(1, true, "Null"));
		}
	}

	public boolean inWilderness() {
		Tile tile = getTile();
		return tile.x > 2941 && tile.x < 3329 && tile.z > 3523 && tile.z < 3968;
	}

	public boolean inSafeArea() {
		// Edgeville bank
		Tile tile = getTile();
		return tile.x >= 3091 && tile.x <= 3098 && tile.z >= 3488 && tile.z <= 3499;
	}

	public void precycle() {
		// Sync inventory
		/*if (inventory.dirty()) {
			write(new SetItems(93, 149, 0, inventory));
			inventory.clean();
		}

		// Sync equipment if dirty
		if (equipment.dirty()) {
			write(new SetItems(94, equipment));
			looks.update();
			equipment.clean();

			// Also send the stuff required to make the weaponry panel proper
			updateWeaponInterface();
		}

		// Sync bank if dirty
		if (bank.isDirty()) {
			write(new SetItems(95, bank.getBankItems()));
			bank.clean();
		}*/
	}

	public boolean drainSpecialEnergy(int amount) {
		turnOffSpecialAttack();
		if (getSpecialEnergyAmount() < amount * 10) {
			message("You do not have enough special energy.");
			return false;
		}
		setSpecialEnergyAmount(getSpecialEnergyAmount() - (amount * 10));
		return true;
	}

	public void updateWeaponInterface() {
		Item wep = equipment.get(EquipSlot.WEAPON);
		write(new InterfaceText(593, 1, wep == null ? "Unarmed" : wep.definition(world).name));
		write(new InterfaceText(593, 2, "Combat Lvl: " + skills.combatLevel()));

		// Set the varp that holds our weapon interface panel type
		int panel = wep == null ? 0 : world.equipmentInfo().weaponType(wep.getId());
		varps.setVarp(843, panel);
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	@Override
	public boolean isNpc() {
		return false;
	}

	@Override
	protected void die() {
		world.getEventHandler().addEvent(this, false, new PlayerDeathEvent(this));
	}

	public void write(Object... o) {
		if (channel.isActive()) {
			for (Object msg : o) {
				channel.write(msg);
			}
		}
	}

	public boolean inCombat() {
		return timers.has(TimerKey.IN_COMBAT);
	}

	public boolean canBeAttackInSafeArea() {
		return timers.has(TimerKey.AFTER_COMBAT);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", id).add("username", username).add("displayName", displayName)
				.add("tile", tile).add("privilege", privilege).toString();
	}

	//////////////////

	public void teleportWithAnimation(int x, int y) {
		teleportWithAnimation(x, y, 0);
	}

	public void teleportWithAnimation(int x, int y, int level) {
		teleport(new Tile(x, y));
	}

	public void teleport(Tile tile) {
		if (locked()) {
			return;
		}
		world().getEventHandler().addEvent(this, new TeleportEvent(this, tile));
	}

	@Override
	public int getAttackSound() {
		Item item = getEquipment().get(EquipSlot.WEAPON);
		int soundId = 24;

		if (item == null) {
			return soundId;
		}
		if (StringUtils.containsIgnoreCase(item.definition(world).name, "shortbow")
				|| StringUtils.containsIgnoreCase(item.definition(world).name, "longbow")) {
			soundId = 2693;
		}
		if (StringUtils.containsIgnoreCase(item.definition(world).name, "crossbow")) {
			soundId = 2700;
		}
		if (StringUtils.containsIgnoreCase(item.definition(world).name, "2h")) {
			soundId = 2503;
		}
		if (StringUtils.containsIgnoreCase(item.definition(world).name, "axe")) {
			soundId = 2508;
		}
		if (StringUtils.containsIgnoreCase(item.definition(world).name, "knife")) {
			soundId = 2696;
		}
		if (StringUtils.containsIgnoreCase(item.definition(world).name, "dagger")) {
			soundId = getVarps().getVarp(Varp.ATTACK_STYLE) == 3 ? 2548 : 2547;
		}
		if (StringUtils.containsIgnoreCase(item.definition(world).name, "staff")) {
			soundId = 2555;
		}
		if (StringUtils.containsIgnoreCase(item.definition(world).name, "staff")) {
			soundId = 2547;
		}

		switch (item.getId()) {
		case 4151:
		case 12006:
			soundId = 2720;
			break;
		}
		return soundId;
	}

	@Override
	public int getBlockSound() {
		Item item = getEquipment().get(EquipSlot.SHIELD);
		int soundId = 791;
		if (item == null) {
			soundId = 23;
		} else {
			switch (item.getId()) {
			case 8850:
				soundId = 15;
				break;
			}
		}
		return soundId;
	}

	public void savePlayer() {
		world().server().service(PlayerSerializer.class, true).get().savePlayer(this);
	}

	@Override
	public int getBlockAnim() {
		int animationId = 424;
		Item shield = ((Player) this).getEquipment().get(EquipSlot.SHIELD);
		Item weapon = ((Player) this).getEquipment().get(EquipSlot.WEAPON);
		if (shield != null) {
			switch (shield.getId()) {
			// shields 1156

			// Defenders
			case 8850:
				animationId = 4177;
				break;
			}
		}

		else if (weapon != null) {
			switch (weapon.getId()) {

			// Gmaul
			case 4153:
				animationId = 1666;
				break;

			}
		}

		return animationId;
	}

	public void messageFilterable(String format, Object... params) {
		write(new AddMessage(params.length > 0 ? String.format(format, (Object[]) params) : format,
				AddMessage.Type.GAME_FILTER));
	}

	public void messageDebug(String format, Object... params) {
		if (isDebug()) {
			// message(format, params);
			messageFilterable(format, params);
		}
	}

	public void disableAutocasting() {
		getVarps().setVarbit(Varbit.AUTOCAST, 0);
		getVarps().setVarbit(Varbit.AUTOCAST_SPELL, 0);
		setAutoCasting(false);
	}

	public int getAutoCastingSpellChild() {
		return autoCastingSpellChild;
	}

	public void setAutoCastingSpellChild(int autoCastingSpellChild) {
		this.autoCastingSpellChild = autoCastingSpellChild;
	}

	public boolean hasReceivedStarter() {
		return receivedStarter;
	}

	public void setReceivedStarter(boolean receivedStarter) {
		this.receivedStarter = receivedStarter;
	}

	public void spawnMelee() {
		getInventory().empty();
		getInventory().add(5698);
		getInventory().add(145);
		getInventory().add(157);
		getInventory().add(163);
		getInventory().add(4153);
		getInventory().add(new Item(385, 23));

		getEquipment().set(EquipSlot.HEAD, new Item(10828));
		getEquipment().set(EquipSlot.CAPE, new Item(6570));
		getEquipment().set(EquipSlot.AMULET, new Item(6585));
		getEquipment().set(EquipSlot.WEAPON, new Item(12006));
		getEquipment().set(EquipSlot.BODY, new Item(10551));
		getEquipment().set(EquipSlot.SHIELD, new Item(12954));
		getEquipment().set(EquipSlot.LEGS, new Item(4722));
		getEquipment().set(EquipSlot.HANDS, new Item(7462));
		getEquipment().set(EquipSlot.FEET, new Item(11840));
		getEquipment().set(EquipSlot.RING, new Item(11773));
		// player.getEquipment().set(EquipSlot.AMMO, item);

		setMaster();
		getVarps().setVarbit(Varbit.SPELLBOOK, 2); // lunar
	}

	public void spawnRanged() {
		getInventory().empty();
		getInventory().add(11802);
		getInventory().add(2442);
		getInventory().add(169);
		getInventory().add(2444);
		getInventory().add(new Item(560, 1000));
		getInventory().add(new Item(557, 1000));
		getInventory().add(new Item(9075, 1000));
		getInventory().add(2434);
		getInventory().add(2434);
		getInventory().add(163);
		getInventory().add(new Item(391, 18));

		getEquipment().set(EquipSlot.HEAD, new Item(4753));
		getEquipment().set(EquipSlot.CAPE, new Item(10499));
		getEquipment().set(EquipSlot.AMULET, new Item(6585));
		getEquipment().set(EquipSlot.WEAPON, new Item(9185));
		getEquipment().set(EquipSlot.BODY, new Item(2503));
		getEquipment().set(EquipSlot.SHIELD, new Item(11283));
		getEquipment().set(EquipSlot.LEGS, new Item(4759));
		getEquipment().set(EquipSlot.HANDS, new Item(7462));
		getEquipment().set(EquipSlot.FEET, new Item(2577));
		getEquipment().set(EquipSlot.RING, new Item(6733));
		getEquipment().set(EquipSlot.AMMO, new Item(9244, 1000));

		setMaster();
		getVarps().setVarbit(Varbit.SPELLBOOK, 2); // Lunar

	}

	public void spawnHybrid() {
		getInventory().empty();
		getInventory().add(5698);
		getInventory().add(157);
		getInventory().add(163);
		getInventory().add(145);
		getInventory().add(6570);
		getInventory().add(12006);
		getInventory().add(12954);
		getInventory().add(10551);
		getInventory().add(4722);
		getInventory().add(11840);
		getInventory().add(4736);
		getInventory().add(new Item(560, 10000));
		getInventory().add(new Item(565, 10000));
		getInventory().add(new Item(555, 10000));
		getInventory().add(new Item(397, 14));

		getEquipment().set(EquipSlot.HEAD, new Item(10828));
		getEquipment().set(EquipSlot.CAPE, new Item(2412));
		getEquipment().set(EquipSlot.AMULET, new Item(6585));
		getEquipment().set(EquipSlot.WEAPON, new Item(4675));
		getEquipment().set(EquipSlot.BODY, new Item(4712));
		getEquipment().set(EquipSlot.SHIELD, new Item(6889));
		getEquipment().set(EquipSlot.LEGS, new Item(4714));
		getEquipment().set(EquipSlot.HANDS, new Item(7462));
		getEquipment().set(EquipSlot.FEET, new Item(6920));
		getEquipment().set(EquipSlot.RING, new Item(11773));

		setMaster();
		getVarps().setVarbit(Varbit.SPELLBOOK, 1); // Ancients
	}
}
