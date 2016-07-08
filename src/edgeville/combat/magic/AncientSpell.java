package edgeville.combat.magic;

import edgeville.model.entity.Npc;
import edgeville.model.entity.Player;
import edgeville.model.item.Item;
import edgeville.util.TextUtil;

public enum AncientSpell implements Spell {
	// Ancients
	SMOKE_RUSH(50, 30, 1978, -1, 12, 385, 14, 4, new Item[] { new Item(Runes.CHAOS_RUNE, 2), new Item(Runes.DEATH_RUNE, 2), new Item(Runes.FIRE_RUNE, 1), new Item(Runes.AIR_RUNE, 1) }),
	SHADOW_RUSH(52, 31, 1978, -1, 12, 379, 15, 4, new Item[] { new Item(Runes.CHAOS_RUNE, 2), new Item(Runes.DEATH_RUNE, 2), new Item(Runes.AIR_RUNE, 1), new Item(Runes.SOUL_RUNE, 1) }),
	BLOOD_RUSH(56, 33, 1978, -1, 12, 373, 16, 4, new Item[] { new Item(Runes.CHAOS_RUNE, 2), new Item(Runes.DEATH_RUNE, 2), new Item(Runes.BLOOD_RUNE, 1) }),
	ICE_RUSH(58, 34, 1978, 360, 12, 361, 17, 4, new Item[] { new Item(Runes.CHAOS_RUNE, 2), new Item(Runes.DEATH_RUNE, 2), new Item(Runes.WATER_RUNE, 2) }),

	SMOKE_BURST(62, 36, 1979, -1, 12, 382, 19, 5, new Item[] { new Item(Runes.CHAOS_RUNE, 4), new Item(Runes.DEATH_RUNE, 2), new Item(Runes.FIRE_RUNE, 2), new Item(Runes.AIR_RUNE, 2) }),
	SHADOW_BURST(64, 37, 1979, -1, 12, 382, 19, 5, new Item[] { new Item(Runes.CHAOS_RUNE, 4), new Item(Runes.DEATH_RUNE, 2), new Item(Runes.AIR_RUNE, 1), new Item(Runes.SOUL_RUNE, 2) }),
	BLOOD_BURST(68, 39, 1979, -1, 12, 376, 21, 5, new Item[] { new Item(Runes.CHAOS_RUNE, 4), new Item(Runes.DEATH_RUNE, 2), new Item(Runes.BLOOD_RUNE, 2) }),
	ICE_BURST(70, 40, 1979, -1, 12, 363, 22, 5, 1126, new Item[] { new Item(Runes.CHAOS_RUNE, 4), new Item(Runes.DEATH_RUNE, 2), new Item(Runes.WATER_RUNE, 4) }),

	SMOKE_BLITZ(74, 42, 1978, -1, 12, 387, 23, 4, new Item[] { new Item(Runes.DEATH_RUNE, 2), new Item(Runes.BLOOD_RUNE, 2), new Item(Runes.FIRE_RUNE, 2), new Item(Runes.AIR_RUNE, 2) }),
	SHADOW_BLITZ(76, 43, 1978, -1, 12, 381, 24, 4, new Item[] { new Item(Runes.DEATH_RUNE, 2), new Item(Runes.BLOOD_RUNE, 2), new Item(Runes.AIR_RUNE, 2), new Item(Runes.SOUL_RUNE, 2) }),
	BLOOD_BLITZ(80, 45, 1978, -1, 12, 375, 25, 4, new Item[] { new Item(Runes.DEATH_RUNE, 2), new Item(Runes.BLOOD_RUNE, 4) }),
	ICE_BLITZ(82, 46, 1978, 366, 12, 367, 26, 4, new Item[] { new Item(Runes.DEATH_RUNE, 2), new Item(Runes.BLOOD_RUNE, 2), new Item(Runes.WATER_RUNE, 3) }),

	SMOKE_BARRAGE(86, 48, 1978, -1, 12, 391, 27, 5, new Item[] { new Item(Runes.DEATH_RUNE, 4), new Item(Runes.BLOOD_RUNE, 2), new Item(Runes.FIRE_RUNE, 4), new Item(Runes.AIR_RUNE, 4) }),
	SHADOW_BARRAGE(88, 49, 1979, -1, 12, 383, 28, 5, new Item[] { new Item(Runes.DEATH_RUNE, 4), new Item(Runes.BLOOD_RUNE, 2), new Item(Runes.AIR_RUNE, 4), new Item(Runes.SOUL_RUNE, 3) }),
	BLOOD_BARRAGE(92, 51, 1979, -1, 12, 377, 29, 5, new Item[] { new Item(Runes.DEATH_RUNE, 4), new Item(Runes.BLOOD_RUNE, 4), new Item(Runes.SOUL_RUNE, 1) }),
	ICE_BARRAGE(94, 52, 1979, 366, 12, 369, 30, 5, 1125, new Item[] { new Item(Runes.DEATH_RUNE, 4), new Item(Runes.BLOOD_RUNE, 2), new Item(Runes.WATER_RUNE, 6) });

	private int levelReq;
	private int animation;
	private int projectileId;
	private int speed;
	private int gfx;
	private int gfxOther;
	private int maxHit;
	private int combatDelayTicks;
	private int soundIdOnImpact;
	private Item[] runesRequired;
	private int baseMagicXp;

	AncientSpell(int levelReq, int baseMagicXp, int animation, int projectileId, int speed, int gfx, int maxHit, int combatDelayTicks, Item[] runesRequired) {
		this.levelReq = levelReq;
		this.animation = animation;
		this.projectileId = projectileId;
		this.speed = speed;
		this.gfx = gfx;
		this.maxHit = maxHit;
		this.combatDelayTicks = combatDelayTicks;
		this.runesRequired = runesRequired;
		this.baseMagicXp = baseMagicXp;
	}

	AncientSpell(int levelReq, int baseMagicXp, int animation, int projectileId, int speed, int gfx, int maxHit, int combatDelayTicks, int soundIdOnImpact, Item[] runesRequired) {
		this.levelReq = levelReq;
		this.animation = animation;
		this.projectileId = projectileId;
		this.speed = speed;
		this.gfx = gfx;
		this.maxHit = maxHit;
		this.combatDelayTicks = combatDelayTicks;
		this.soundIdOnImpact = soundIdOnImpact;
		this.runesRequired = runesRequired;
		this.baseMagicXp = baseMagicXp;
	}

	public boolean hasRunes(Player player) {
		boolean nope = true;
		for (Item item : getRunesRequired()) {
			if (!player.getInventory().contains(item.getId(), item.getAmount())) {
				player.message("YOU DO NOT HAVE THE RUNES: " + item.definition(player.world()).name);
				// return false;
				nope = false;
			}
		}
		if (!nope)
			return false;
		return true;
	}

	public void removeRunes(Player player) {
		for (Item item : getRunesRequired()) {
			player.getInventory().remove(item);
		}
	}

	public int getAnimation() {
		return animation;
	}

	public int getProjectileId() {
		return projectileId;
	}

	public int getSpeed() {
		return speed;
	}

	public int getGfx() {
		return gfx;
	}

	public int getMaxHit() {
		return maxHit;
	}

	public int getCombatDelayTicks() {
		return combatDelayTicks;
	}

	public int getSoundId() {
		return soundIdOnImpact;
	}

	public int getLevelReq() {
		return levelReq;
	}

	public Item[] getRunesRequired() {
		return runesRequired;
	}

	@Override
	public String toString() {
		return TextUtil.formatEnum(name());
	}

	public int getBaseMagicXp() {
		return baseMagicXp;
	}

	public void setBaseMagicXp(int baseMagicXp) {
		this.baseMagicXp = baseMagicXp;
	}

	public static AncientSpell getByName(String spellName) {
		return valueOf(spellName);
	}
}
