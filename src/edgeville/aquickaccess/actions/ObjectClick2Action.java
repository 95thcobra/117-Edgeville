package edgeville.aquickaccess.actions;

import edgeville.model.AttributeKey;
import edgeville.model.entity.Player;
import edgeville.model.map.MapObj;

/**
 * Created by Sky on 28-6-2016.
 */
public class ObjectClick2Action {
	public void handleObjectClick(Player player, MapObj mapObj) {
		switch (mapObj.id()) {

		// Bank
		case 11744:
			player.getBank().open();
			break;

		// Unhandled objects
		default:
			if (player.isDebug()) {
				player.message("Unhandled object click 2: " + mapObj.id());
			}
			break;
		}
	}
}
