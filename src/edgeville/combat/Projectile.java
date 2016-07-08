package edgeville.combat;

import org.apache.commons.lang3.StringUtils;

public enum Projectile {
	TRAINING_ARROW(9706, "training arrow", 16, new Graphic(25, 92, 0)),
	BRONZE_ARROW(882, "bronze arrow", 10, new Graphic(19, 92, 0), new Graphic(1104, 92, 0)),
	IRON_ARROW(884, "iron arrow", 9, new Graphic(18, 92, 0), new Graphic(1105, 92, 0)),
	STEEL_ARROW(886, "steel arrow", 11, new Graphic(20, 92, 0), new Graphic(1106, 92, 0)),
	// BLACK_ARROW(14),
	MITH_ARROW(888, "mithril arrow", 12, new Graphic(21, 92, 0), new Graphic(1107, 92, 0)),
	ADDY_ARROW(890, "adamant arrow", 13, new Graphic(22, 92, 0), new Graphic(1108, 92, 0)),
	DRAGON_ARROW(11212, "dragon arrow", 17, new Graphic(26, 92, 0), new Graphic(1111, 92, 0)),
	RUNE_ARROW(892, "rune arrow", 15, new Graphic(24, 92, 0), new Graphic(1109, 92, 0)),

	BRONZE_KNIFE(864, "bronze knife", 219, new Graphic(219, 92, 0)),
	IRON_KNIFE(863, "iron knife", 213, new Graphic(220, 92, 0)),
	STEEL_KNIFE(865, "steel knife", 214, new Graphic(221, 92, 0)),
	BLACK_KNIFE(869, "black knife", 215, new Graphic(222, 92, 0)),
	MITHRIL_KNIFE(866, "mithril knife", 216, new Graphic(223, 92, 0)),
	ADAMANT_KNIFE(867, "adamant knife", 217, new Graphic(224, 92, 0)),
	RUNE_KNIFE(868, "rune knife", 218, new Graphic(225, 92, 0)),

	BRONZE_DART("bronze dart", 226, new Graphic(232, 105, 7)),
	IRON_DART("iron dart", 227, new Graphic(233, 105, 7)),
	STEEL_DART("steel dart", 228, new Graphic(234, 105, 7)),
	MITH_DART("mithril dart", 229, new Graphic(235, 105, 7)),
	ADAMANT_DART("adamant dart", 230, new Graphic(236, 105, 7)),
	RUNE_DART("rune dart", 231, new Graphic(237, 105, 7)),
	DRAGON_DART("dragon dart", 1122, new Graphic(1123, 105, 7)),
	
	//CHIN("chinchompa", 908, null),
	BLACK_CHIN("black chinchompa", 908, null),
	RED_CHIN("red chinchompa", 909, null),
	
	OBBY_RING("toktz-xil-ul", 442, null)
	
	;

	private int itemId;// SHOULD NOT BE USED ANYMORE
	private String itemName;
	private int projectileId;
	private Graphic gfx;
	private Graphic darkBowGfx;

	Projectile(String itemName, int projectileId, Graphic gfx) {
		this.itemId = -1;
		this.itemName = itemName;
		this.projectileId = projectileId;
		this.gfx = gfx;
	}
	
	Projectile(int itemId, String itemName, int projectileId, Graphic gfx) {
		this.itemId = itemId;
		this.itemName = itemName;
		this.projectileId = projectileId;
		this.gfx = gfx;
	}
	
	Projectile(int itemId, String itemName, int projectileId, Graphic gfx, Graphic darkBowGfx) {
		this.itemId = itemId;
		this.itemName = itemName;
		this.projectileId = projectileId;
		this.gfx = gfx;
		this.darkBowGfx = darkBowGfx;
	}

	/*
	 * Projectile(int itemId, String itemName, int projectileId) { this.itemId =
	 * itemId; this.itemName = itemName; this.gfx = gfx; }
	 */

	public static Projectile getProjectileForAmmoId(int itemId) {
		for (Projectile projectile : values()) {
			if (projectile.itemId == itemId) {
				return projectile;
			}
		}
		return null;
	}

	public static Projectile getProjectileForAmmoName(String itemName) {
		for (Projectile projectile : values()) {
			if (StringUtils.containsIgnoreCase(itemName, projectile.itemName)) {
				return projectile;
			}
		}
		return null;
	}

	public int getItemId() {
		return itemId;
	}

	public int getProjectileId() {
		return projectileId;
	}
	
	public Graphic getDarkBowGfx() {
		return darkBowGfx;
	}

	public Graphic getGfx() {
		return gfx;
	}
}
