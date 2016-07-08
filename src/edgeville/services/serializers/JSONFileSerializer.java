package edgeville.services.serializers;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import edgeville.bank.BankTab;
import edgeville.combat.magic.AncientSpell;
import edgeville.combat.magic.RegularDamageSpell;
import edgeville.combat.magic.Spell;
import edgeville.model.AttributeKey;
import edgeville.model.Tile;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Bank;
import edgeville.model.entity.player.Privilege;
import edgeville.model.entity.player.Skills;
import edgeville.model.entity.player.skills.Prayer;
import edgeville.model.entity.player.skills.Prayers;
import edgeville.model.item.Item;
import edgeville.model.uid.UIDProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Simon on 5-3-2015.
 *         <p>
 *         Simple default serializer to <b>only</b> use in single-server setups
 *         because it uses a local file to serialize the player data to by means
 *         of GSON.
 */
public class JSONFileSerializer extends PlayerSerializer {

	private static final Logger logger = LogManager.getLogger(JSONFileSerializer.class);

	private Gson gson;

	/**
	 * The folder containing the character files.
	 */
	private File characterFolder = new File("data/characters");

	public JSONFileSerializer(UIDProvider provider) {
		super(provider);

		gson = new GsonBuilder().setPrettyPrinting().create(); // TODO
																// configuration

		/* Create folder if missing */
		characterFolder.mkdirs();
	}

	@Override
	public boolean loadPlayer(Player player, Object uid, String password, Consumer<PlayerLoadResult> fn) {
		File characterFile = new File(characterFolder, player.getUsername() + ".json");

		// If the file does not exist, let the caller know.
		if (!characterFile.exists()) {
			fn.accept(PlayerLoadResult.OK);
			return true;
		}

		try {
			fn.accept(loadPlayer(player, new FileInputStream(characterFile), password));
		} catch (FileNotFoundException e) {
			logger.error("Could not decode JSON player data for {} because the file was missing!", player.name(), e);
			fn.accept(PlayerLoadResult.INVALID_DETAILS);
		}

		return true;
	}

	public PlayerLoadResult loadPlayer(Player player, InputStream inputStream, String password) {
		JsonElement element = new JsonParser().parse(new InputStreamReader(inputStream));

		if (element.isJsonObject()) {
			JsonObject rootObject = element.getAsJsonObject();

			// Check password
			if (!rootObject.get("password").getAsString().equals(password))
				return PlayerLoadResult.INVALID_DETAILS;

			/* Basic information */
			String displayName = rootObject.get("displayName").getAsString();

			boolean receivedStarter = rootObject.get("receivedStarter").getAsBoolean();
			player.setReceivedStarter(receivedStarter);

			Privilege privilege = Privilege.valueOf(rootObject.get("privilege").getAsString());
			Tile tile = gson.fromJson(rootObject.get("tile"), Tile.class);
			int migration = rootObject.get("migration").getAsInt();
			JsonElement kills = rootObject.get("kills");
			JsonElement deaths = rootObject.get("deaths");
			player.setKills(kills.getAsInt());
			player.setDeaths(deaths.getAsInt());

			// Debug
			player.setDebug(rootObject.get("debug").getAsBoolean());

			/* Construct the player */
			player.displayName(displayName);
			player.privilege(privilege);
			player.migration(migration);
			player.move(tile);

			player.setTile(tile);

			/* Skill information */
			JsonArray skills = rootObject.get("skills").getAsJsonArray();
			for (int i = 0; i < skills.size(); i++) {
				JsonObject skill = skills.get(i).getAsJsonObject();
				player.skills().levels()[i] = skill.get("level").getAsInt();
				player.skills().xp()[i] = skill.get("xp").getAsInt();
			}
			player.skills().recalculateCombat();

			/* inventory */
			JsonArray inventory = rootObject.get("inventory").getAsJsonArray();
			for (int i = 0; i < player.getInventory().size(); i++) {
				Item item = gson.fromJson(inventory.get(i), Item.class);
				player.getInventory().set(i, item);
			}

			/* equipment */
			JsonArray equipment = rootObject.get("equipment").getAsJsonArray();
			for (int i = 0; i < player.getEquipment().size(); i++) {
				Item item = gson.fromJson(equipment.get(i), Item.class);
				player.getEquipment().set(i, item);
			}

			/* load out */
			JsonObject loadout = rootObject.get("loadout").getAsJsonObject();
			JsonArray inv = loadout.get("inventory").getAsJsonArray();
			for (int i = 0; i < player.getLoadout().getInventory().length; i++) {
				player.getLoadout().getInventory()[i] = gson.fromJson(inv.get(i), Item.class);
			}
			JsonArray equip = loadout.get("equipment").getAsJsonArray();
			for (int i = 0; i < player.getLoadout().getEquipment().length; i++) {
				player.getLoadout().getEquipment()[i] = gson.fromJson(equip.get(i), Item.class);
			}

			// bank
			JsonArray bankArray = rootObject.get("bank").getAsJsonArray();
			for (JsonElement jElement : bankArray) {
				Item item = gson.fromJson(jElement, Item.class);
				player.getBank().getBankItems().add(item);
			}

			/* varps */
			JsonArray varps = rootObject.get("varps").getAsJsonArray();
			for (int i = 0; i < varps.size(); i++) {
				JsonObject varp = varps.get(i).getAsJsonObject();
				player.getVarps().setVarp(varp.get("id").getAsInt(), varp.get("value").getAsInt());
			}

			// Skull head icon
			JsonElement skullIcon = rootObject.get("skullIcon");
			if (skullIcon != null) {
				player.setSkullHeadIcon(skullIcon.getAsInt());
			}

			// Prayer head icon
			JsonElement prayerIcon = rootObject.get("prayerIcon");
			if (prayerIcon != null) {
				player.setPrayerHeadIcon(prayerIcon.getAsInt());
			}

			// Quick prayers
			JsonArray quickPrayers = rootObject.get("quickprayers").getAsJsonArray();
			for (JsonElement ele : quickPrayers) {
				Prayers prayer = Prayers.valueOf(ele.getAsString());
				player.getPrayer().getQuickPrayers().add(prayer);
			}

			// autocasting spell
			JsonElement aSpell = rootObject.get("autocastSpell");
			int aChild = rootObject.get("autocastSpellChild").getAsInt();
			JsonElement lcSpell = rootObject.get("lastCastedSpell");
			int lcChild = rootObject.get("lastCastedSpellChild").getAsInt();
			boolean autocasting = rootObject.get("autocasting").getAsBoolean();

			player.setAutoCastingSpellChild(aChild);
			player.setLastSpellCastChild(lcChild);
			player.setAutoCasting(autocasting);

			if (aSpell != null) {
				Spell spell;
				try {
					spell = AncientSpell.valueOf(aSpell.getAsString());
				} catch (Exception e) {
					spell = RegularDamageSpell.valueOf(aSpell.getAsString());
				}
				if (spell != null) {
					player.setAutoCastingSpell(spell);
				}
			}

			if (lcSpell != null) {
				Spell lastSpell;
				try {
					lastSpell = AncientSpell.valueOf(lcSpell.getAsString());
				} catch (Exception e) {
					lastSpell = RegularDamageSpell.valueOf(lcSpell.getAsString());
				}
				if (lastSpell != null) {
					player.setLastCastedSpell(lastSpell);
				}
			}

			return PlayerLoadResult.OK;
		}

		return PlayerLoadResult.INVALID_DETAILS;
	}

	@Override
	public void savePlayer(Player player) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("username", player.getUsername());
		jsonObject.addProperty("displayName", player.getDisplayName());
		jsonObject.addProperty("password", player.getPassword());
		jsonObject.addProperty("receivedStarter", player.hasReceivedStarter());
		jsonObject.add("tile", gson.toJsonTree(player.getTile()));
		jsonObject.add("privilege", gson.toJsonTree(player.getPrivilege()));
		jsonObject.addProperty("migration", player.migration());
		jsonObject.addProperty("skullIcon", player.getSkullHeadIcon());
		jsonObject.addProperty("prayerIcon", player.getPrayerHeadIcon());
		jsonObject.addProperty("debug", player.isDebug());
		jsonObject.addProperty("kills", player.getKills());
		jsonObject.addProperty("deaths", player.getDeaths());

		/* Inventory */
		JsonArray inventory = new JsonArray();
		for (int i = 0; i < player.getInventory().size(); i++) {
			inventory.add(gson.toJsonTree(player.getInventory().get(i)));
		}
		jsonObject.add("inventory", inventory);

		/* equipment */
		JsonArray equipment = new JsonArray();
		for (int i = 0; i < player.getEquipment().size(); i++) {
			equipment.add(gson.toJsonTree(player.getEquipment().get(i)));
		}
		jsonObject.add("equipment", equipment);

		/* loadout */
		JsonObject loadout = new JsonObject();
		JsonArray inv = new JsonArray();
		for (int i = 0; i < player.getInventory().size(); i++) {
			inv.add(gson.toJsonTree(player.getLoadout().getInventory()[i]));
		}
		JsonArray equip = new JsonArray();
		for (int i = 0; i < player.getEquipment().size(); i++) {
			equip.add(gson.toJsonTree(player.getLoadout().getEquipment()[i]));
		}
		loadout.add("inventory", inv);
		loadout.add("equipment", equip);
		jsonObject.add("loadout", loadout);

		// Bank
		/*
		 * JsonObject bank = new JsonObject(); // Iterate over every bank tab.
		 * for (int i = 0; i < player.getBank().getBankTabs().length; i++) {
		 * BankTab bankTab = player.getBank().getBankTabs()[i];
		 * 
		 * // If bank tab empty, skip. if (bankTab.getItems().isEmpty()) {
		 * continue; }
		 * 
		 * // Array of items in bank tab. JsonArray items = new JsonArray(); for
		 * (Item item : bankTab.getItems()) { items.add(gson.toJsonTree(item));
		 * }
		 * 
		 * //jsonBankTab.add(""+i, items); bank.add("" + i, items); }
		 * jsonObject.add("bank", bank);
		 */

		// Bank
		JsonArray jsonBank = new JsonArray();
		// Iterate over every bank tab.
		List<Item> bankItems = player.getBank().getBankItems();
		for (Item item : bankItems) {
			jsonBank.add(gson.toJsonTree(item));
		}
		jsonObject.add("bank", jsonBank);

		/* varps */
		JsonArray varps = new JsonArray();
		for (int i = 0; i < player.getVarps().getVarps().length; i++) {
			int varppp = player.getVarps().getVarps()[i];
			if (varppp == 0) {
				continue;
			}
			JsonObject varp = new JsonObject();
			varp.addProperty("id", i);
			varp.addProperty("value", varppp);
			varps.add(varp);
		}
		jsonObject.add("varps", varps);

		/* Skills */
		JsonArray skills = new JsonArray();
		for (int i = 0; i < Skills.SKILL_COUNT; i++) {
			JsonObject object = new JsonObject();
			object.addProperty("level", player.skills().level(i));
			object.addProperty("xp", player.skills().xp()[i]);
			skills.add(object);
		}
		jsonObject.add("skills", skills);

		/* prayers */
		JsonArray quickprayers = new JsonArray();
		for (int i = 0; i < player.getPrayer().getQuickPrayers().size(); i++) {
			Prayers prayer = player.getPrayer().getQuickPrayers().get(i);
			quickprayers.add(gson.toJsonTree(prayer));
		}
		jsonObject.add("quickprayers", quickprayers);

		// autocasting spell
		jsonObject.add("autocastSpell", gson.toJsonTree(player.getAutoCastingSpell()));
		// autocasting child
		jsonObject.addProperty("autocastSpellChild", player.getAutoCastingSpellChild());
		// last casted spell
		jsonObject.add("lastCastedSpell", gson.toJsonTree(player.getLastCastedSpell()));
		// last casted child
		jsonObject.addProperty("lastCastedSpellChild", player.getLastSpellCastChild());
		// autocasting
		jsonObject.addProperty("autocasting", player.isAutoCasting());

		// end

		File characterFile = new File(characterFolder, player.getUsername() + ".json");
		try (FileWriter out = new FileWriter(characterFile)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			out.write(gson.toJson(jsonObject));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
