package edgeville.script;

/**
 * @author Simon on 8/13/2015.
 */
public enum TimerKey {

	/**
	 * Key used when eating food.
	 */
	FOOD,

	/**
	 * Key used when delaying hits with combat.
	 */
	COMBAT_ATTACK,
	
	/**
	 * Key used when player is in combat.
	 */
	IN_COMBAT,
	
	/**
	 * Key used when player runs into safe area.
	 */
	AFTER_COMBAT,

	/**
	 * Key used when burying bones in the ground.
	 */
	BONE_BURYING,

	/**
	 * Key used when cleaning herbs.
	 */
	HERB_CLEANING,

	/**
	 * Key used when cutting gems.
	 */
	GEM_CUTTING,

	/**
	 * Key used when drinking potions
	 */
	POTION,

	/**
	 * Key used to recharge special energy, 10% every 30 seconds (aka 50 ticks).
	 */
	SPECIAL_ENERGY_RECHARGE,

	/**
	 * Key used to indicate an entity is currently frozen and blocked from moving.
	 */
	FROZEN,

	/**
	 * Key used when replenishing decreased or increased stats. Fired once per minute.
	 */
	STAT_REPLENISH,

	/**
	 * Key used to indicate that the entity is currently stunned.
	 */
	STUNNED,

	/*
	 * Key used to indivate that the vengeance spell is on cooldown
	 */
	VENGEANCE_COOLDOWN,
	
	DFS_COOLDOWN,

	PRAYER_TICK,
	
	SKULL, KARAMBWAN
	
	

}
