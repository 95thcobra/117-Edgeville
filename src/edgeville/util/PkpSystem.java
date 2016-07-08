package edgeville.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edgeville.model.AttributeKey;
import edgeville.model.entity.Player;

/**
 * Created by Carl on 2015-08-22.
 */
public class PkpSystem {

	public static HashMap<Player, List<String>> ips = new HashMap<>();

	public static void addPoints(Player player, int points) {
		player.putAttribute(AttributeKey.PK_POINTS, (int) player.attribute(AttributeKey.PK_POINTS, 0) + points);
	}

	public static int getPoints(Player player) {
		return player.attribute(AttributeKey.PK_POINTS, 0);
	}


	public static void handleDeath(Player killer, Player target) {
		List<String> listedIps = ips.getOrDefault(killer, new ArrayList<>());

		if (!listedIps.contains(target.channel().remoteAddress().toString())) {
			listedIps.add(target.channel().remoteAddress().toString());

			addPoints(killer, 1);
			killer.message("You receive 1 PK point for killing " + target.name() + " for a total of " + getPoints(killer) + " points.");
		} else {
			killer.message("You don't receive any points for killing " + target.name() + " as you have recently fought them.");
		}

		if (listedIps.size() == 3)
			listedIps.remove(0);

		ips.put(killer, listedIps);
	}

	public static int getCost(int itemId) {
		switch (itemId) {
			case 2579: //Wizard boots
			case 11840: //Dragon boots
			case 12829: //Spirit shield
			case 8842: //Void gloves
			case 11832: //Bandos chestplate
			case 11834: //Bandos tassets
			case 11836: //Bandos boots
			case 11826: //Armadyl helmet
			case 11828: //Armadyl chestplate
			case 11830: //Armadyl chainskirt
				return 20;
			case 6920: //Infinity boots
			case 12612: //Book of darkness
			case 12924: //Dragon defender
			case 11663: //Void mage helm
			case 11664: //Void ranger helm
			case 11665: //Void melee helm
			case 11674: //Void mage helm
			case 11675: //Void ranger helm
			case 11676: //Void melee helm
			case 6889: //Mage's book
				return 25;
			case 10547: //Healer hat
			case 10548: //Fighter hat
			case 6914: //Master wand
			case 11804: //Bandos godsword
			case 11806: //Saradomin godsword
			case 11808: //Zamorak godsword
				return 30;
			case 10551: //Fighter torso
			case 6570: //Fire cape
			case 11235: //Dark bow
			case 12831: //Blessed spirit shield
			case 8839: //Void melee top
			case 8840: //Void knight robe
			case 6731: //Seers ring
			case 6733: //Archers ring
			case 11802: //Armadyl godsword
				return 50;
			case 11759: //New crystal shield (full)
			case 13072: //Elite void top
			case 13073: //Elite void robe
				return 75;
			case 6737: //Berserker ring
			case 6585: //Amulet of Fury
			case 12002: //Occult necklace
			case 2577: //Ranger boots
			case 12006: //Abyssal tentacle
				return 100;
			case 11924: //Malediction ward
			case 11926: //Odium ward
				return 200;
			case 12825: //Arcane spirit shield
				return 300;
			case 12821: //Spectral spirit shield
				return 400;
			case 12817: //Elysian spirit shield
				return 450;
			default:
				return -1;
		}
	}

}
