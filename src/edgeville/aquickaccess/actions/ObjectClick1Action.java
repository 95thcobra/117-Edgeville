package edgeville.aquickaccess.actions;

import edgeville.aquickaccess.dialogue.DialogueHandler;
import edgeville.model.AttributeKey;
import edgeville.model.entity.Player;
import edgeville.model.map.MapObj;

/**
 * Created by Sky on 21-6-2016.
 */
public class ObjectClick1Action {
	public void handleObjectClick(Player player, MapObj mapObj) {
		switch (mapObj.id()) {

		// Spellbook altar edgeville
		case 6552:
			new DialogueHandler().sendOptionDialogue(player, "Choose your spellbook", "Regular", "Ancient", "Lunar", "Maybe later");
			player.setDialogueAction(2);
			break;

		// Prayer altar edgeville
		case 6817:
			player.skills().restorePrayer();
			player.animate(645);
			player.message("You have recharged your prayer.");
			break;

		// Wilderness ditch
		case 23271:
			boolean below = player.getTile().z <= 3520;
			int targetY = (below ? 3523 : 3520);
			player.move(player.getTile().x, targetY);
			break;
			
		// Unhandled objects
		default:
			if (player.isDebug()) {
				player.message("Unhandled object click 1: " + mapObj.id());
			}
			break;
		}
	}
}
