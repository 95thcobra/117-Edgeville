package edgeville.model.entity.player;

import edgeville.model.entity.Player;
import edgeville.model.item.Item;
import edgeville.model.item.ItemContainer;

public class Loadout {
	private Item[] inventory;
	private Item[] equipment;

	public Loadout() {
		inventory = new Item[28];
		equipment = new Item[14];
	}

	public Item[] getInventory() {
		return inventory;
	}

	public void setInventory(Item[] inventory) {
		this.inventory = inventory;
	}

	public Item[] getEquipment() {
		return equipment;
	}

	public void setEquipment(Item[] equipment) {
		this.equipment = equipment;
	}

	public void save(Player player) {
		ItemContainer inv = player.getInventory();
		for (int i = 0; i < inv.size(); i++) {
			inventory[i] = inv.get(i);
		}

		ItemContainer equip = player.getEquipment();
		for (int i = 0; i < equip.size(); i++) {
			equipment[i] = equip.get(i);
		}
	}

	public void load(Player player) {
		ItemContainer inv = player.getInventory();
		for (int i = 0; i < inv.size(); i++) {
			inv.set(i, inventory[i]);
		}

		ItemContainer equip = player.getEquipment();
		for (int i = 0; i < equip.size(); i++) {
			equip.set(i, equipment[i]);
		}
	}
}
