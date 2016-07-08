package edgeville.combat;

import edgeville.model.entity.player.Skills;

/**
 * Created by Sky on 28-6-2016.
 */
public enum Potions {
    ATTACK_POTION(Skills.ATTACK, 3, 10, true, "Attack potion", 2428, 121, 123, 125),

    SUPER_ATTACK_POTION(Skills.ATTACK, 5, 15, true, "Super attack potion", 2436, 145, 147, 149),

    STRENGTH_POTION(Skills.STRENGTH, 3, 10, true, "Strength potion", 113, 115, 117, 119),

    SUPER_STRENGTH_POTION(Skills.STRENGTH, 5, 15, true, "Super strength potion", 2440, 157, 159, 161),

    DEFENCE_POTION(Skills.DEFENCE, 3, 10, true, "Defence potion", 2432, 133, 135, 137),

    SUPER_DEFENCE_POTION(Skills.DEFENCE, 5, 15, true, "Super defence potion", 2442, 163, 165, 167),

    MAGIC_POTION(Skills.MAGIC, 3, 10, true, "Magic potion", 3040, 3042, 3044, 3046),

    SUPER_MAGIC_POTION(Skills.MAGIC, 2, 12, true, "Super magic potion", 11726, 11727, 11728, 11729),

    RANGING_POTION(Skills.RANGED, 3, 10, true, "Ranging potion", 2444, 169, 171, 173),

    SUPER_RANGING_POTION(Skills.RANGED, 2, 12, true, "Super ranging potion", 11722, 11723, 11724, 11725),

    SARADOMIN_BREW(-1, -1, -1, false, "Saradomin brew", 6685, 6687, 6689, 6691),
    ZAMORAK_BREW(-1, -1, -1, false, "Zamorak brew", 2450, 189, 191, 193),

    
    SUPER_RESTORE(-1, -1, -1, true, "Super restore", 3024, 3026, 3028, 3030),
    PRAYER_POTION(-1, -1, -1, true, "Prayer potion", 2434, 139, 141, 143),
    
    
    

    RESTORE_POTION(-1, -1, -1, false, "Restore potion", 2430, 127, 129, 131),

    COMBAT_POTION(-1, -1, -1, false, "Combat potion", 9739, 9741, 9743, 9745);

    private int skill;
    private int baseValue;
    private int percentage;
    private boolean normal;
    private String name;
    private int[] ids;

    Potions(int skill, int baseValue, int percentage, boolean normal, String name, int... ids) {
        this.skill = skill;
        this.baseValue = baseValue;
        this.percentage = percentage;
        this.normal = normal;
        this.name = name;
        this.ids = ids;
    }

    public int getNextDose(int id) {
        for(int i = 0 ; i < ids.length; i++) {
            if (ids[i] == id) {
                return ids[i + 1];
            }
        }
        return 0;
    }

    public boolean isLastDose(int potionId) {
        return ids[ids.length - 1] == potionId;
    }

    public int dosesLeft(int potionId) {
        for(int i = 0 ; i < ids.length; i++) {
            if (ids[i] == potionId) {
                return ids.length - i - 1;
            }
        }
        return 0;
    }

    public int getSkill() {
        return skill;
    }

    public int getBaseValue() {
        return baseValue;
    }

    public int getPercentage() {
        return percentage;
    }

    public boolean isNormal() {
        return normal;
    }

    public String getName() {
        return name;
    }

    public int[] getIds() {
        return ids;
    }
}
