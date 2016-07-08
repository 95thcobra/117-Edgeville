package edgeville.util;

import edgeville.model.Entity;
import edgeville.model.entity.Npc;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.EquipSlot;
import edgeville.model.entity.player.Skills;
import edgeville.model.entity.player.WeaponType;
import edgeville.model.entity.player.skills.Prayers;
import edgeville.model.item.Item;

/**
 * @author Simon on 8/15/2015.
 */
public class CombatFormula {

	public static boolean willHit(Entity damager, Entity receiver, CombatStyle style) {
		if (damager.isPlayer() && receiver.isPlayer()) {
			Player player = ((Player) damager);
			Player target = ((Player) receiver);
			EquipmentInfo.Bonuses playerBonuses = totalBonuses(player, player.world().equipmentInfo());
			EquipmentInfo.Bonuses targetBonuses = totalBonuses(target, player.world().equipmentInfo());

			if (style == CombatStyle.MELEE) {
				double praymod = 1;
				double voidbonus = 1;
				double E = Math.floor(((player.skills().level(Skills.ATTACK) * praymod) + 8) * voidbonus);
				double E_ = Math.floor(((target.skills().level(Skills.DEFENCE) * praymod) + 8) * voidbonus);

				int meleebonus = Math.max(Math.max(playerBonuses.crush, playerBonuses.stab), playerBonuses.slash);
				int meleedef = Math.max(Math.max(targetBonuses.crushdef, targetBonuses.stabdef), targetBonuses.slashdef);
				double A = E * (1 + (meleebonus) / 64.);
				double D = E_ * (1 + (meleedef) / 64.);

				double roll = A < D ? ((A - 1) / (2 * D)) : (1 - (D + 1) / (2 * A));
				return Math.random() <= roll;
			} else if (style == CombatStyle.RANGED) {
				double praymod = 1;
				double voidbonus = 1;
				double E = Math.floor(((player.skills().level(Skills.RANGED) * praymod) + 8) * voidbonus);
				double E_ = Math.floor(((target.skills().level(Skills.DEFENCE) * praymod) + 8) * voidbonus);

				double A = E * (1 + (playerBonuses.range) / 64.);
				double D = E_ * (1 + (targetBonuses.rangedef) / 64.);

				double roll = A < D ? ((A - 1) / (2 * D)) : (1 - (D + 1) / (2 * A));
				return Math.random() <= roll;
			} else if (style == CombatStyle.MAGIC) {
				double praymod = 1;
				double voidbonus = 1;
				double E = Math.floor(((player.skills().level(Skills.MAGIC) * praymod) + 8) * voidbonus);
				double E_M = Math.floor(((target.skills().level(Skills.MAGIC) * praymod) + 8) * voidbonus) * 0.3;
				double E_D = Math.floor(((target.skills().level(Skills.DEFENCE) * praymod) + 8) * voidbonus) * 0.7;
				double E_D2 = Math.floor(((target.skills().level(Skills.DEFENCE) * praymod) + 8) * voidbonus);
				double E_ = E_M + E_D;

				double A = E * (1 + (playerBonuses.mage) / 64.);
				double D = E_ * (1 + (targetBonuses.magedef) / 64.);

				double roll = A < D ? ((A - 1) / (2 * D)) : (1 - (D + 1) / (2 * A));
				return Math.random() <= roll;
			}
		}

		return false;
	}

	/**
	 * Not used!
	 * @param player
	 * @param combatStyle
	 * @return
	 */
	public static int siminMaxHit(Player player, CombatStyle combatStyle) {
		double strength = Math.floor(player.skills().level(Skills.STRENGTH));
		double base = 1.00;

		// Prayer multipliers
		if (combatStyle == CombatStyle.MELEE) {
			base *= prayerMeleeMultiplier(player);
			// Black mask: slayer only
			// Salve amulet: vs undead
			// Salve amulet (e) : vs undead
			// void knight melee
		} else if (combatStyle == CombatStyle.RANGED) {
			base *= prayerRangedMultiplier(player);
			// void knight ranged
		}

		int effectiveStrength = (int) Math.floor(strength * base);

		if (combatStyle == CombatStyle.MELEE) {
			effectiveStrength += extraDamageBasedOnAttackStyleMelee(player);
		} else if (combatStyle == CombatStyle.RANGED) {
			effectiveStrength += extraDamageBasedOnAttackStyleRanged(player);
		}
		
		//TODO DHAROK
		
		return effectiveStrength;
	}
	
	public static int maximumMeleeHit(Player player) {
		EquipmentInfo.Bonuses bonuses = totalBonuses(player, player.world().equipmentInfo());

		double effectiveStr = Math.floor(player.skills().level(Skills.STRENGTH));
		effectiveStr *= prayerMeleeMultiplier(player); // PRAYER BY SKY
		
		// TODO effectiveStr depends on prayer and style and e.g. salve ammy
		double baseDamage = 1.3 + (effectiveStr / 10d) + (bonuses.str / 80d) + ((effectiveStr * bonuses.str) / 640d);

		if (fullDharok(player)) {
			double hp = player.hp();
			double max = player.maxHp();
			double mult = Math.max(0, ((max - hp) / max) * 100d) + 100d;
			baseDamage *= (mult / 100);
		}

		if (hasGodSword(player))
			baseDamage *= 1.1;
		// TODO some more special handling etc for e.g. ags.. or do we do that
		// in the override in cb?

		return (int) baseDamage;
	}

	public static int extraDamageBasedOnAttackStyleRanged(Player player) {
		int extraDmg = 0;

		int book = player.getVarps().getVarp(843); // weapon style
		int style = player.getVarps().getVarp(43);

		switch (book) {
		// all atm
		default:
			if (style == 0) {
				extraDmg = 3;
			}
			break;
		}

		return extraDmg;
	}

	public static int extraDamageBasedOnAttackStyleMelee(Player player) {
		int extraDmg = 0;

		int book = player.getVarps().getVarp(843); // weapon style
		int style = player.getVarps().getVarp(43);

		switch (book) {
		// no weapon
		case 0:
			// Aggressive
			if (style == 1) {
				extraDmg = 3;
			}
			break;
		// Whip
		case 20:
			if (style == 1) {
				extraDmg = 1;
			}
			break;
		}

		return extraDmg;
	}

	private static double prayerRangedMultiplier(Player player) {
		double base = 1.00;
		// Prayer multipliers
		if (player.getPrayer().isPrayerOn(Prayers.SHARP_EYE)) {
			base *= 1.05;
		}
		if (player.getPrayer().isPrayerOn(Prayers.HAWK_EYE)) {
			base *= 1.10;
		}
		if (player.getPrayer().isPrayerOn(Prayers.EAGLE_EYE)) {
			base *= 1.15;
		}
		return base;
	}

	private static double prayerMeleeMultiplier(Player player) {
		double base = 1.00;
		// Prayer multipliers
		if (player.getPrayer().isPrayerOn(Prayers.BURST_OF_STRENGTH)) {
			base *= 1.05;
		}
		if (player.getPrayer().isPrayerOn(Prayers.SUPERHUMAN_STRENGTH)) {
			base *= 1.10;
		}
		if (player.getPrayer().isPrayerOn(Prayers.ULTIMATE_STRENGTH)) {
			base *= 1.15;
		}
		if (player.getPrayer().isPrayerOn(Prayers.CHILVAlRY)) {
			base *= 1.18;
		}
		if (player.getPrayer().isPrayerOn(Prayers.PIETY)) {
			base *= 1.23;
		}
		return base;
	}


	public static int maximumRangedHit(Player player) {
		EquipmentInfo.Bonuses bonuses = totalBonuses(player, player.world().equipmentInfo());

		double effectiveStr = Math.floor(player.skills().level(Skills.RANGED));
		effectiveStr *= prayerRangedMultiplier(player); // PRAYER BY SKY

		// TODO effectiveStr depends on prayer and style and e.g. salve ammy
		double baseDamage = 1.3 + (effectiveStr / 10d) + (bonuses.rangestr / 80d) + ((effectiveStr * bonuses.rangestr) / 640d);

		return (int) baseDamage;
	}

	public static EquipmentInfo.Bonuses totalBonuses(Entity entity, EquipmentInfo info) {
		EquipmentInfo.Bonuses bonuses = new EquipmentInfo.Bonuses();

		if (entity instanceof Player) {
			Player player = (Player) entity;

			for (int i = 0; i < 14; i++) {
				Item equipped = player.getEquipment().get(i);
				if (equipped != null) {
					EquipmentInfo.Bonuses equip = info.bonuses(equipped.getId());

					bonuses.stab += equip.stab;
					bonuses.slash += equip.slash;
					bonuses.crush += equip.crush;
					bonuses.range += equip.range;
					bonuses.mage += equip.mage;

					bonuses.stabdef += equip.stabdef;
					bonuses.slashdef += equip.slashdef;
					bonuses.crushdef += equip.crushdef;
					bonuses.rangedef += equip.rangedef;
					bonuses.magedef += equip.magedef;

					bonuses.str += equip.str;
					bonuses.rangestr += equip.rangestr;
					bonuses.magestr += equip.magestr;
					bonuses.pray += equip.pray;
				}
			}
		} else {
			/* Nothing as of right now. */
		}

		return bonuses;
	}

	private static boolean fullDharok(Player player) {
		return player.getEquipment().hasAny(4718, 4886, 4887, 4888, 4889) && // Axe
				player.getEquipment().hasAny(4716, 4880, 4881, 4882, 4883) && // Helm
				player.getEquipment().hasAny(4720, 4892, 4893, 4894, 4895) && // Body
				player.getEquipment().hasAny(4722, 4898, 4899, 4900, 4901); // Legs
	}

	private static boolean hasGodSword(Player player) {
		return player.getEquipment().hasAny(11802, 11804, 11806, 11808);
	}

}
