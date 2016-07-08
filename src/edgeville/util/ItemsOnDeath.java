package edgeville.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import edgeville.fs.ItemDefinition;
import edgeville.model.GroundItem;
import edgeville.model.entity.Player;
import edgeville.model.item.Item;

/**
 * Created by Carl on 2015-08-23.
 */
public class ItemsOnDeath {

	public static void dropItems(Player killer, Player target) {

		ArrayList<Item> items = new ArrayList<>();
		ArrayList<Item> keptItems = new ArrayList<>();

		Collections.addAll(items, target.getInventory().copy());
		Collections.addAll(items, target.getEquipment().copy());

		Collections.sort(items, (o1, o2) -> {
			if (o1 == null || o2 == null)
				return 0;

			ItemDefinition def = o1.definition(killer.world());
			ItemDefinition def2 = o2.definition(killer.world());
			if (def == null || def2 == null)
				return -1;
			if (def.cost * o1.getAmount() < def2.cost * o2.getAmount())
				return 1;
			return -1;
		});

		target.getInventory().empty();
		target.getEquipment().empty();

		for (int i = 0; i < (items.size() < 3 ? items.size() : 3); i++) {
			if (items.get(i) != null) {
				keptItems.add(items.get(i));
				target.getInventory().add(items.get(i), true);
			}
		}

		//killer.message("SIZE:"+items.size());

		items.stream().filter(i -> i != null && !keptItems.contains(i)).forEach(i -> {
			killer.world().spawnGroundItem(new GroundItem(i, target.getTile(), killer));
			//killer.message("ITEM"+i.id());
		});

		killer.world().spawnGroundItem(new GroundItem(new Item(526), target.getTile(), killer));
	}

}
