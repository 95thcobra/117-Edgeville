package edgeville.model;

/**
 * @author Simon on 8/13/2015.
 */
public enum AttributeKey {

	LAST_ATTACKED_BY,
	
	/**
	 * Key indicating if this player is having debugging enabled.
	 */
	//DEBUG,

	/**
	 * Key indicating if this player is trying to log out.
	 */
	LOGOUT,

	TARGET,

	LAST_REGION,

	ITEM_SLOT,

	BUTTON_SLOT,

	/**
	 * Key used to indicate which action (1..10) was used when button pressing.
	 */
	BUTTON_ACTION,

	/*
	 * Key used to indicate that the vengeance spell is active
	 */
	VENGEANCE_ACTIVE,

	/*
	 *
	 */
	PK_POINTS,

	/**
	 * Key used to indicate which ground item we are trying to take.
	 */
	//GROUNDITEM_TARGET,

	/**
	 * Key used to indicate which map object we are interacting with.
	 */
	INTERACTION_OBJECT,

	/**
	 * Key used to indicate which option we're using on the object
	 */
	INTERACTION_OPTION,

	/**
	 * Key used to have a 0..100 value which holds the current prayer incremental status to have sub-tick increments.
	 */
	PRAYERINCREMENT,

	/**
	 * Key uesd to indicate who the latest player attacking another player was
	 */
	LAST_DAMAGER,

	/**
	 *  Key used to indicate when a player received the latest hit
	 */
	LAST_DAMAGE,

	/**
	 *  Key used to indicate combat target type, 0 = player, 1 = npc
	 */
	TARGET_TYPE

}
