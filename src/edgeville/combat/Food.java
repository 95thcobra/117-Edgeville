package edgeville.combat;

import edgeville.util.TextUtil;

/**
 * Created by Sky on 28-6-2016.
 */
public enum Food {

    SHRIMP(315, 3, -1),
    COOKED_CHICKEN(2140, 4, -1),
    SARDINE(325, 4, -1),
    COOKED_MEAT(2142, 4, -1),
    BREAD(2309, 5, -1),
    HERRING(347, 5, -1),
    MACKEREL(355, 6, -1),
    TROUT(333, 7, -1),
    SALMON(329, 9, -1),
    TUNA(361, 10, -1),

    CAKE(1891, 4, 1893),
    TWO_THIRDS_CAKE(1893, 4, 1895),
    ONE_THIRD_CAKE(1895, 4, -1),

    LOBSTER(379, 12, -1),
    BASS(365, 13, -1),
    SWORDFISH(373, 14, -1),
    MONKFISH(7946, 16, -1),
    ANCHOVY_PIZZA(2297, 9, 2299),
    HALF_ANCHOVY_PIZZA(2299, 9, -1),

    SHARK(385, 20, -1),
    //SHARK(6969, 20, -1),

    SEA_TURTLE(397, 21, -1),
    MANTA_RAY(391, 22, -1),
    TUNA_POTATO(7060, 22, -1),
    DARK_CRAB(11936, 22, -1),
    REDBERRY_PIE(2325, 5, 2333),
    HALF_REDBERRY_PIE(2333, 5, 2313),
    MEAT_PIE(2327, 6, 2331),
    HALF_MEAT_PIE(2331, 6, 2313),
    APPLE_PIE(2323, 7, 2335),
    HALF_APPLE_PIE(2335, 7, 2313),
    GARDEN_PIE(7178, 6, 7180),
    HALF_GARDEN_PIE(7180, 6, 2313),
    FISH_PIE(7188, 6, 7190),
    HALF_FISH_PIE(7190, 6, 2313),
    ADMIRAL_PIE(7198, 8, 7200),
    HALF_ADMIRAL_PIE(7200, 8, 2313),
    WILD_PIE(7208, 11, 7210),
    HALF_WILD_PIE(7210, 11, 2313),
    SUMMER_PIE(7218, 11, 7220),
    HALF_SUMMER_PIE(7220, 11, 2313),
    BEER(1917, 1, 1919),
	
    COOKED_KARAMBWAN(3144, 18, -1);

    private int id;
    private int heal;
    private int nextId;

    Food(int id, int heal, int nextId) {
        this.id = id;
        this.heal = heal;
        this.nextId = nextId;
    }

    public int getId() {
        return id;
    }

    public int getHeal() {
        return heal;
    }

    public int getNextId() {
        return nextId;
    }
    
    @Override
    public String toString() {
    	return TextUtil.formatEnum(name());
    }
}
