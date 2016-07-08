package edgeville.combat.magic;

import edgeville.model.entity.Player;
import edgeville.model.item.Item;
import edgeville.util.TextUtil;

public enum RegularDamageSpell implements Spell {
	WIND_STRIKE(1, 5.5, 711, 91, 12, 90, 92, 2, 4, new Item[] { new Item(Runes.AIR_RUNE), new Item(Runes.MIND_RUNE) }),
	WATER_STRIKE(5,7.5, 711, 94, 12, 93, 95, 4, 4, new Item[] { new Item(Runes.WATER_RUNE),new Item(Runes.AIR_RUNE), new Item(Runes.MIND_RUNE) }),
	EARTH_STRIKE(9,9.5, 711, 97, 12, 96, 98, 6, 4, new Item[] { new Item(Runes.EARTH_RUNE,2),new Item(Runes.AIR_RUNE), new Item(Runes.MIND_RUNE) }),
	FIRE_STRIKE(13,11.5, 711, 100, 12, 99, 101, 8, 4, new Item[] { new Item(Runes.FIRE_RUNE,3),new Item(Runes.AIR_RUNE,2), new Item(Runes.MIND_RUNE) }),
	
	WIND_BOLT(17, 13.5,	711, 118, 12, 	117, 119, 9, 4, new Item[] { new Item(Runes.AIR_RUNE,2),new Item(Runes.CHAOS_RUNE,1) }),
	WATER_BOLT(23, 16.5,	711, 121, 12, 	120, 122, 10, 4, new Item[] { new Item(Runes.WATER_RUNE,2),new Item(Runes.AIR_RUNE,2), new Item(Runes.CHAOS_RUNE) }),
	EARTH_BOLT(29,19.5, 	711, 124, 12, 	123, 125, 11, 4, new Item[] { new Item(Runes.EARTH_RUNE,3),new Item(Runes.AIR_RUNE,2), new Item(Runes.CHAOS_RUNE) }),
	FIRE_BOLT(35, 21.5,	711, 127, 12, 	126, 128, 12, 4, new Item[] { new Item(Runes.FIRE_RUNE,4),new Item(Runes.AIR_RUNE,3), new Item(Runes.CHAOS_RUNE) }),
	
	WIND_BLAST(41, 24.5,	711, 133, 12, 132, 134, 13, 4, new Item[] { new Item(Runes.AIR_RUNE,3), new Item(Runes.DEATH_RUNE) }),
	WATER_BLAST(47,25.5, 711, 136, 12, 135, 137, 14, 4, new Item[] { new Item(Runes.WATER_RUNE,3),new Item(Runes.AIR_RUNE,3), new Item(Runes.DEATH_RUNE) }),
	EARTH_BLAST(53,28.5, 711, 139, 12, 138, 140, 15, 4, new Item[] { new Item(Runes.EARTH_RUNE,4),new Item(Runes.AIR_RUNE,3), new Item(Runes.DEATH_RUNE) }),
	FIRE_BLAST(59, 30,	711, 130, 12, 129, 131, 16, 4, new Item[] { new Item(Runes.FIRE_RUNE,5),new Item(Runes.AIR_RUNE,4), new Item(Runes.DEATH_RUNE) }),
	
	WIND_WAVE(62, 36,	711, 159, 12, 158, 160, 17, 4, new Item[] { new Item(Runes.AIR_RUNE,5), new Item(Runes.BLOOD_RUNE) }),
	WATER_WAVE(65,37.5, 	711, 162, 12, 161, 163, 18, 4, new Item[] { new Item(Runes.WATER_RUNE,7),new Item(Runes.AIR_RUNE,5), new Item(Runes.BLOOD_RUNE) }),
	EARTH_WAVE(70,40, 	711, 165, 12, 164, 166, 19, 4, new Item[] { new Item(Runes.EARTH_RUNE,7),new Item(Runes.AIR_RUNE,5), new Item(Runes.BLOOD_RUNE) }),
	FIRE_WAVE(75,42.5, 	711, 156, 12, 155, 157, 20, 4, new Item[] { new Item(Runes.FIRE_RUNE,7),new Item(Runes.AIR_RUNE,5), new Item(Runes.BLOOD_RUNE) }),
	
	;

	private int levelReq;
	private double magicExperience;
	private int animation;
	private int projectileId;
	private int projectileSpeed;
	private int gfx;
	private int gfxOther;
	private int maxHit;
	private int combatDelayTicks;
	private Item[] runesRequired;

	RegularDamageSpell(int levelReq, double magicExperience, int animation, int projectileId, int projectileSpeed, int gfx, int gfxOther, int maxHit, int combatDelayTicks, Item[] runesRequired) {
		this.levelReq = levelReq;
		this.animation = animation;
		this.projectileId = projectileId;
		this.projectileSpeed = projectileSpeed;
		this.gfx = gfx;
		this.gfxOther = gfxOther;
		this.maxHit = maxHit;
		this.combatDelayTicks = combatDelayTicks;
		this.runesRequired = runesRequired;this.magicExperience=magicExperience;
	}

	public int getLevelReq() {
		return levelReq;
	}

	public int getAnimation() {
		return animation;
	}

	public int getProjectileId() {
		return projectileId;
	}

	public int getProjectileSpeed() {
		return projectileSpeed;
	}

	public int getGfx() {
		return gfx;
	}

	public int getGfxOther() {
		return gfxOther;
	}

	public int getMaxHit() {
		return maxHit;
	}

	public int getCombatDelayTicks() {
		return combatDelayTicks;
	}

	public Item[] getRunesRequired() {
		return runesRequired;
	}

	
	public double getMagicExperience() {
		return magicExperience;
	}

	public boolean hasRunes(Player player) {
		for (Item item : getRunesRequired()) {
			if (!player.getInventory().contains(item.getId(), item.getAmount())) {
				player.message("YOU DO NOT HAVE THE RUNES: " + item.definition(player.world()).name);
				return false;
			}
		}
		return true;
	}
	
	public void removeRunes(Player player) {
		for (Item item : getRunesRequired()) {
			player.getInventory().remove(item);
		}
	}

	@Override
	public String toString() {
		return TextUtil.formatEnum(name());
	}

	public static RegularDamageSpell getByName(String spellName) {
		return valueOf(spellName);
	}

}
