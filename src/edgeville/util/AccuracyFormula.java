package edgeville.util;

import java.security.SecureRandom;

import edgeville.model.Entity;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Skills;

/**
 * @author Simon on 8/22/2015.
 */
public class AccuracyFormula {

    public static final SecureRandom srand = new SecureRandom();

    public static void main(String[] args) {
    }

    public static boolean doesHit(Player player, Entity enemy, CombatStyle style) {
        EquipmentInfo.Bonuses playerBonuses = CombatFormula.totalBonuses(player, player.world().equipmentInfo());
        EquipmentInfo.Bonuses targetBonuses = CombatFormula.totalBonuses(enemy, player.world().equipmentInfo());

		/*
            S E T T I N G S

			S T A R T
		*/

        //attack stances
        int off_stance_bonus = 0; //accurate, aggressive, controlled, defensive
        int def_stance_bonus = 0; //accurate, aggressive, controlled, defensive

        //requirements
        int off_weapon_requirement = 1; //weapon attack level requirement
        int off_spell_requirement = 1; //spell magic level requirement

        //base levels
        int off_base_attack_level = (int) (player.skills().xpLevel(Skills.ATTACK) * 1.5);
        int off_base_ranged_level = player.skills().xpLevel(Skills.RANGED);
        int off_base_magic_level = player.skills().xpLevel(Skills.MAGIC);

        //current levels
        double off_current_attack_level = player.skills().level(Skills.ATTACK) * 1.5;
        double off_current_ranged_level = player.skills().level(Skills.RANGED);
        double off_current_magic_level = player.skills().level(Skills.MAGIC);

        double def_current_defence_level = 1, def_current_magic_level = 1;
        if (enemy instanceof Player) {
            Player enemenyPlayer = (Player) enemy;

            def_current_defence_level = enemenyPlayer.skills().level(Skills.DEFENCE);
            def_current_magic_level = enemenyPlayer.skills().level(Skills.MAGIC);
        }

        //prayer bonuses
        double off_attack_prayer_bonus = 1.0;
        double off_ranged_prayer_bonus = 1.0;
        double off_magic_prayer_bonus = 1.0;
        double def_defence_prayer_bonus = 1.0;

        //additional bonus
        double off_additional_bonus = 1.0;

        //equipment bonuses
        int off_equipment_stab_attack = playerBonuses.stab;
        int off_equipment_slash_attack = playerBonuses.slash;
        int off_equipment_crush_attack = playerBonuses.crush;
        int off_equipment_ranged_attack = playerBonuses.range;
        int off_equipment_magic_attack = playerBonuses.mage;

        int def_equipment_stab_defence = targetBonuses.stabdef;
        int def_equipment_slash_defence = targetBonuses.slashdef;
        int def_equipment_crush_defence = targetBonuses.crushdef;
        int def_equipment_ranged_defence = targetBonuses.rangedef;
        int def_equipment_magic_defence = targetBonuses.magedef;

        //protect from * prayers
        boolean def_protect_from_melee = player.getVarps().getVarbit(Varbit.PROTECT_FROM_MELEE) == 1;
        boolean def_protect_from_ranged = player.getVarps().getVarbit(Varbit.PROTECT_FROM_MISSILES) == 1;
        boolean def_protect_from_magic = player.getVarps().getVarbit(Varbit.PROTECT_FROM_MAGIC) == 1;

        //chance bonuses
        double off_special_attack_bonus = 1.0;
        double off_void_bonus = 1.0;

		/*
			S E T T I N G S

			E N D
		*/



		/*
			C A L C U L A T E D
			V A R I A B L E S

			S T A R T
		*/

        //experience bonuses
        double off_spell_bonus = 0;
        double off_weapon_bonus = 0;

        //effective levels
        double effective_attack = 0;
        double effective_magic = 0;
        double effective_defence = 0;

        //relevent equipment bonuses
        int off_equipment_bonus = 0;
        int def_equipment_bonus = 0;

        //augmented levels
        double augmented_attack = 0;
        double augmented_defence = 0;

        //hit chances
        double hit_chance = 0;
        double off_hit_chance = 0;
        double def_block_chance = 0;

		/*
			C A L C U L A T E D
			V A R I A B L E S

			E N D
		*/


        //determine effective attack
        switch (style) {
            case MELEE:
                if (off_base_attack_level > off_weapon_requirement) {
                    off_weapon_bonus = (off_base_attack_level - off_weapon_requirement) * .3;
                }

                effective_attack = Math.floor(((off_current_attack_level * off_attack_prayer_bonus) * off_additional_bonus) + off_stance_bonus + off_weapon_bonus);
                effective_defence = Math.floor((def_current_defence_level * def_defence_prayer_bonus) + def_stance_bonus);

					/*switch(off_style) {
						case "stab":
							off_equipment_bonus = off_equipment_stab_attack;
							def_equipment_bonus = def_equipment_stab_defence;
							break;
						case "slash":
							off_equipment_bonus = off_equipment_slash_attack;
							def_equipment_bonus = def_equipment_slash_defence;
							break;
						case "crush":
							off_equipment_bonus = off_equipment_crush_attack;
							def_equipment_bonus = def_equipment_crush_defence;
							break;
					}*/

                off_equipment_bonus = Math.max(Math.max(off_equipment_stab_attack, off_equipment_slash_attack), off_equipment_crush_attack);
                def_equipment_bonus = Math.max(Math.max(def_equipment_stab_defence, def_equipment_slash_defence), def_equipment_crush_defence);
                break;
            case RANGED:
                if (off_base_ranged_level > off_weapon_requirement) {
                    off_weapon_bonus = (off_base_ranged_level - off_weapon_requirement) * .3;
                }
                effective_attack = Math.floor(((off_current_ranged_level * off_ranged_prayer_bonus) * off_additional_bonus) + off_stance_bonus + off_weapon_bonus);
                effective_defence = Math.floor((def_current_defence_level * def_defence_prayer_bonus) + def_stance_bonus);
                off_equipment_bonus = off_equipment_ranged_attack;
                def_equipment_bonus = def_equipment_ranged_defence;
                break;
            case MAGIC:
                if (off_base_magic_level > off_spell_requirement) {
                    off_spell_bonus = (off_base_magic_level - off_spell_requirement) * .3;
                }
                effective_attack = Math.floor(((off_current_magic_level * off_magic_prayer_bonus) * off_additional_bonus) + off_spell_bonus);
                effective_magic = Math.floor(def_current_magic_level * .7);
                effective_defence = Math.floor((def_current_defence_level * def_defence_prayer_bonus) * .3);
                effective_defence = effective_defence + effective_magic;
                off_equipment_bonus = off_equipment_magic_attack;
                def_equipment_bonus = def_equipment_magic_defence;
                break;
        }

        //determine augmented levels
        augmented_attack = Math.floor(((effective_attack + 8) * (off_equipment_bonus + 64.)) / 10.);
        augmented_defence = Math.floor(((effective_defence + 8) * (def_equipment_bonus + 64.)) / 10.);

        //determine hit chance
        if (augmented_attack < augmented_defence) {
            hit_chance = (augmented_attack - 1) / (augmented_defence * 2);
        } else {
            hit_chance = 1 - ((augmented_defence + 1) / (augmented_attack * 2));
        }

        switch (style) {
            case MELEE:
                if (def_protect_from_melee) {
                    off_hit_chance = Math.floor((((hit_chance * off_special_attack_bonus) * off_void_bonus) * .6) * 100.);
                    def_block_chance = Math.floor(101 - ((((hit_chance * off_special_attack_bonus) * off_void_bonus) * .6) * 100.));
                } else {
                    off_hit_chance = Math.floor(((hit_chance * off_special_attack_bonus) * off_void_bonus) * 100.);
                    def_block_chance = Math.floor(101 - (((hit_chance * off_special_attack_bonus) * off_void_bonus) * 100.));
                }
                break;
            case RANGED:
                if (def_protect_from_ranged) {
                    off_hit_chance = Math.floor((((hit_chance * off_special_attack_bonus) * off_void_bonus) * .6) * 100.);
                    def_block_chance = Math.floor(101 - ((((hit_chance * off_special_attack_bonus) * off_void_bonus) * .6) * 100.));
                } else {
                    off_hit_chance = Math.floor(((hit_chance * off_special_attack_bonus) * off_void_bonus) * 100.);
                    def_block_chance = Math.floor(101 - (((hit_chance * off_special_attack_bonus) * off_void_bonus) * 100.));
                }
                break;
            case MAGIC:
                if (def_protect_from_magic) {
                    off_hit_chance = Math.floor(((hit_chance * off_void_bonus) * .6) * 100.);
                    def_block_chance = Math.floor(101 - (((hit_chance * off_void_bonus) * .6) * 100.));
                } else {
                    off_hit_chance = Math.floor((hit_chance * off_void_bonus) * 100.);
                    def_block_chance = Math.floor(101 - ((hit_chance * off_void_bonus) * 100.));
                    off_hit_chance *= 1.06;
                }
                break;
        }

        //print hit chance
        //System.out.println("\nYour chance to hit is: " + off_hit_chance + "%");
        //System.out.println("Your opponents chance to block is: " + def_block_chance + "%");

        //roll dice
        if (off_hit_chance <= 0)
            off_hit_chance = 2;

        off_hit_chance = srand.nextInt((int) off_hit_chance);
        off_hit_chance *= 1.45;
        def_block_chance = srand.nextInt((int) def_block_chance);

        //print roll
        //System.out.println("\nYou rolled: " + (int) off_hit_chance);
        //System.out.println("Your opponent rolled: " + (int) def_block_chance);

        //determine hit
        return off_hit_chance > def_block_chance;
    } //end main

}
