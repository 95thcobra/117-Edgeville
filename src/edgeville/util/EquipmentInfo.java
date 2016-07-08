package edgeville.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edgeville.aquickaccess.actions.EquipmentRequirement;
import edgeville.aquickaccess.actions.EquipmentRequirement.Skill;
import edgeville.fs.DefinitionRepository;
import edgeville.fs.ItemDefinition;
import edgeville.model.World;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.EquipSlot;
import edgeville.model.entity.player.Skills;
import edgeville.model.entity.player.WeaponType;
import edgeville.model.item.Item;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Simon on 8/14/2015.
 */
public class EquipmentInfo {

	private static final Logger logger = LogManager.getLogger(EquipmentInfo.class);

	private static final int[] DEFAULT_RENDERPAIR = { 808, 823, 819, 820, 821, 822, 824 };
	private static final int[] DEFAULT_WEAPON_RENDERPAIR = { 809, 823, 819, 820, 821, 822, 824 };
	private static final Bonuses DEFAULT_BONUSES = new Bonuses();

	private byte[] slots;
	private byte[] types;
	private Map<Integer, int[]> renderMap = new HashMap<>();
	private Map<Integer, Bonuses> bonuses = new HashMap<>();
	private Map<Integer, Integer> weaponTypes = new HashMap<>();
	private Map<Integer, Integer> weaponSpeeds = new HashMap<>();

	// private Map<Integer, List<EquipmentRequirement>> equipmentRequirements =
	// new HashMap<>();

	public EquipmentInfo(DefinitionRepository repo, File typeSlotFile, File renderPairs, File bonuses, File weaponTypes, File weaponSpeeds, File equipmentRequirements) {
		int numItems = repo.total(ItemDefinition.class);
		slots = new byte[numItems];
		types = new byte[numItems];

		// Set all slots to -1
		for (int i = 0; i < numItems; i++)
			slots[i] = -1;

		loadSlotsAndTypes(typeSlotFile);
		loadRenderPairs(renderPairs);
		loadBonuses(bonuses);
		loadWeaponTypes(weaponTypes);
		loadWeaponSpeeds(weaponSpeeds);

		// loadEquipmentRequirements();
	}

	private void loadSlotsAndTypes(File file) {
		try (Scanner scanner = new Scanner(file)) {
			int numdef = 0;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				int id = Integer.parseInt(line.split(":")[0]);
				String params = line.split(":")[1];
				slots[id] = Byte.parseByte(params.split(",")[0]);
				types[id] = Byte.parseByte(params.split(",")[1]);

				numdef++;
			}

			logger.info("Loaded {} equipment information definitions.", numdef);
		} catch (FileNotFoundException e) {
			logger.error("Could not load equipment information", e);
		}
	}

	private void loadRenderPairs(File file) {
		try (Scanner scanner = new Scanner(file)) {
			int numdef = 0;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				int id = Integer.parseInt(line.split(":")[0]);
				String params[] = line.split(":")[1].split(",");
				int[] pair = new int[7];
				for (int i = 0; i < 7; i++)
					pair[i] = Integer.parseInt(params[i]);
				renderMap.put(id, pair);
				numdef++;
			}

			logger.info("Loaded {} equipment render pairs.", numdef);
		} catch (FileNotFoundException e) {
			logger.error("Could not load render pairs", e);
		}
	}

	private void loadBonuses(File file) {
		try (Scanner scanner = new Scanner(file)) {
			int numdef = 0;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				int id = Integer.parseInt(line.split(":")[0]);
				String params[] = line.split(":")[1].split(",");

				int[] bonuses = new int[5 + 5 + 4];
				for (int i = 0; i < bonuses.length; i++)
					bonuses[i] = Integer.parseInt(params[i]);

				Bonuses b = new Bonuses();
				b.stab = bonuses[0];
				b.slash = bonuses[1];
				b.crush = bonuses[2];
				b.range = bonuses[4];
				b.mage = bonuses[3];

				b.stabdef = bonuses[5];
				b.slashdef = bonuses[6];
				b.crushdef = bonuses[7];
				b.rangedef = bonuses[9];
				b.magedef = bonuses[8];

				b.str = bonuses[10];
				b.rangestr = bonuses[11];
				b.magestr = bonuses[12];
				b.pray = bonuses[13];

				this.bonuses.put(id, b);
				numdef++;
			}

			logger.info("Loaded {} equipment bonuses.", numdef);
		} catch (FileNotFoundException e) {
			logger.error("Could not load bonuses", e);
		}
	}

	private void loadWeaponTypes(File file) {
		try (Scanner scanner = new Scanner(file)) {
			int numdef = 0;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				int id = Integer.parseInt(line.split(":")[0]);
				int type = Integer.parseInt(line.split(":")[1]);

				weaponTypes.put(id, type);
				numdef++;
			}

			logger.info("Loaded {} weapon types.", numdef);
		} catch (FileNotFoundException e) {
			logger.error("Could not load weapon types.", e);
		}
	}

	private void loadWeaponSpeeds(File file) {
		try (Scanner scanner = new Scanner(file)) {
			int numdef = 0;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				int id = Integer.parseInt(line.split(":")[0]);
				int type = Integer.parseInt(line.split(":")[1]);

				weaponSpeeds.put(id, type);
				numdef++;
			}

			logger.info("Loaded {} weapon speeds.", numdef);
		} catch (FileNotFoundException e) {
			logger.error("Could not load weapon speeds.", e);
		}
	}

	/**
	 * Not used, since equip requirements are done with strings.
	 * 
	 * @param file
	 */
	private void loadEquipmentRequirements(File file) {

		String[] skills = { "ATTACK", "DEFENCE ", "STRENGTH ", "HITPOINTS ", "RANGED ", "PRAYER ", "MAGIC ", "COOKING ", "WOODCUTTING ", "FLETCHING ", "FISHING ", "FIREMAKING ", "CRAFTING", "SMITHING ", "MINING", "HERBLORE", "AGILITY", "THIEVING ", "SLAYER", "FARMING ", "RUNECRAFTING ", "HUNTER ",
				"CONSTRUCTION ", "SUMMONING" };

		try (InputStream inputStream = new FileInputStream(file)) {
			JsonElement element = new JsonParser().parse(new InputStreamReader(inputStream));

			int amt = 0;

			JsonArray jsonArray = element.getAsJsonArray();
			for (JsonElement ele : jsonArray) {
				JsonObject jObject = ele.getAsJsonObject();
				int id = jObject.get("id").getAsInt();
				JsonArray reqs = jObject.get("requirements").getAsJsonArray();

				List<EquipmentRequirement> eqReqs = new ArrayList<>();

				for (JsonElement elemen : reqs) {
					JsonObject object1 = elemen.getAsJsonObject();
					int level = object1.get("level").getAsInt();
					String skill = object1.get("skill").getAsString();
					// System.out.println(level + " " + skill);

					int skillIndex = Arrays.asList(skills).indexOf(skill);
					if (skillIndex == -1) {
						continue;
					}

					System.out.println(Skill.valueOf(skill).toString());

					eqReqs.add(new EquipmentRequirement(Skill.valueOf(skill), level));
					amt++;
				}

				// equipmentRequirements.put(id, eqReqs);
			}
			logger.info("Loaded {} equipment requirements.", amt);
		} catch (IOException e) {
			logger.info("Could not load equipment requirements.", e);
		}
	}

	public boolean wearable(int id) {
		return id > 0 && id < slots.length && slots[id] != -1;
	}

	public int slotFor(int id) {
		return slots[id];
	}

	public int typeFor(int id) {
		return types[id];
	}

	public int[] renderPair(int id) {
		if (id == -1)
			return DEFAULT_RENDERPAIR;
		return renderMap.getOrDefault(id, DEFAULT_WEAPON_RENDERPAIR);
	}

	public int weaponType(int id) {
		return weaponTypes.getOrDefault(id, 0);
	}

	public List<EquipmentRequirement> getEquipmentRequirements(Player player, Item item, int targetSlot) {
		String itemName = item.definition(player.world()).name;
		List<EquipmentRequirement> reqs = new ArrayList<EquipmentRequirement>();

		boolean attackRequirementFound = false;

		// Dragon
		if (!attackRequirementFound) {
			if (StringUtils.containsIgnoreCase(itemName, "dragon")) {
				if (targetSlot == EquipSlot.WEAPON)
					reqs.add(new EquipmentRequirement(Skill.ATTACK, 60));
				else
					reqs.add(new EquipmentRequirement(Skill.DEFENCE, 60));
				attackRequirementFound = true;
			}
		}

		// All items that require 70 attack.
		if (!attackRequirementFound) {
			String[] require70Attack = { "abyssal whip" };
			for (String name : require70Attack) {
				if (StringUtils.containsIgnoreCase(itemName, name)) {
					reqs.add(new EquipmentRequirement(Skill.ATTACK, 70));
					attackRequirementFound = true;
				}
			}
		}

		// All items that require 75 attack.
		if (!attackRequirementFound) {
			String[] require75Attack = { "godsword", "abyssal tentacle" };
			for (String name : require75Attack) {
				if (StringUtils.containsIgnoreCase(itemName, name)) {
					reqs.add(new EquipmentRequirement(Skill.ATTACK, 75));
				}
			}
		}
		return reqs;
	}

	public boolean rapid(Player player) {
		int book = player.getVarps().getVarp(843); // weapon book
		int style = player.getVarps().getVarp(43);
		return style == 1 && (book == WeaponType.CROSSBOW || book == WeaponType.BOW
				|| book == WeaponType.THROWN/*
											 * || book == WeaponType.CHINCHOMPA
											 */);
	}

	public static int attackAnimationFor(Player player) {
		int book = player.getVarps().getVarp(843); // weapon style
		int style = player.getVarps().getVarp(43);

		// Handle individual cases first
		int weapon = player.getEquipment().hasAt(EquipSlot.WEAPON) ? player.getEquipment().get(EquipSlot.WEAPON).getId() : 0;
		if (weapon != 0) {
			switch (weapon) {
			case 11802:
			case 11804:
			case 11806:
			case 11808:
				switch (style) {
				case 0:
					return 7045;
				case 1:
					return 7045;
				case 2:
					return 7054;
				case 3:
					return 7055;
				}

				// karil cbow
			case 4734:
				return 2075;
			// obby ring
			case 6522:
				return 1060;

			case 4151: // Abyssal whip
			case 12006: // Abyssal tentacle
			case 12773: // Abyssal lava whip
			case 12774: // Abyssal ice whip
				return 1658;
			case 4718: // Dharok's greataxe
			case 4886: // Dharok's greataxe
			case 4887: // Dharok's greataxe
			case 4888: // Dharok's greataxe
			case 4889: // Dharok's greataxe
				return style == 3 ? 2066 : 2067;
			case 4755: // Verac's flail
			case 4982: // Verac's flail
			case 4983: // Verac's flail
			case 4984: // Verac's flail
			case 4985: // Verac's flail
				return 2062;
			case 4747: // Torag's hamers
			case 4958: // Torag's hamers
			case 4959: // Torag's hamers
			case 4960: // Torag's hamers
			case 4961: // Torag's hamers
				return 2068;
			case 5061: // Toxic blowpipe
				return 5061;
			case 4153: // Granite maul
				return 1665;
			case 6528: // Obsidian maul
				return 2661;
			case 13265:
			case 5698:// dragon dagger (s)
				return style == 0 ? 402 : 451;
			}
		}

		// Then resolve the remaining ones from the guessing based on book type
		switch (book) {
		case WeaponType.CHINCHOMPA:
			return 2779;
		case WeaponType.UNARMED:
			return style == 1 ? 423 : 422;
		case WeaponType.AXE:
			return style == 2 ? 401 : 395;
		case WeaponType.HAMMER:
			return 401;
		case WeaponType.BOW:
			return 426;
		case WeaponType.CROSSBOW:
			return 4230;
		case WeaponType.LONGSWORD:
			return style == 2 ? 386 : 390;
		case WeaponType.TWOHANDED:
			return style == 2 ? 406 : 407;
		case WeaponType.PICKAXE:
			return style == 2 ? 400 : 401;
		case WeaponType.DAGGER:
			return style == 2 ? 390 : 386;
		case WeaponType.MAGIC_STAFF:
			return 419;
		case WeaponType.MACE:
			return style == 2 ? 400 : 401;
		case WeaponType.THROWN:
			return 929;
		}

		return 422; // Fall back to fist fighting so people know it's a wrong
					// anim and (hopefully) report it.
	}

	public Bonuses bonuses(int id) {
		return bonuses.getOrDefault(id, DEFAULT_BONUSES);
	}

	public int weaponSpeed(int id) {
		return weaponSpeeds.getOrDefault(id, 5);
	}

	public static class Bonuses {
		public int stab;
		public int slash;
		public int crush;
		public int range;
		public int mage;
		public int stabdef;
		public int slashdef;
		public int crushdef;
		public int rangedef;
		public int magedef;
		public int str;
		public int rangestr;
		public int magestr;
		public int pray;
	}

}