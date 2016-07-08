package edgeville.bank;

import java.util.ArrayList;
import java.util.List;

import edgeville.model.item.Item;

public class BankTab {
	private int id;
	private int varbit;
	private List<Item> items;

	public BankTab(int id, int varbit) {
		this.id = id;
		this.varbit = varbit;
		this.items = new ArrayList<>();
	}

	public void add(Item item) {
		items.add(item);
	}

	public void remove(Item item) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId() == item.getId()) {
				items.remove(i);
			}
		}
	}

	public boolean contains(int itemId) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId() == itemId) {
				return true;
			}
		}
		return false;
	}

	public boolean contains(int itemId, int amount) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId() == itemId && items.get(i).getAmount() >= amount) {
				return true;
			}
		}
		return false;
	}

	// Removes all and returns how many removed.
	public int remove(int itemId) {
		int amount = -1;
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId() == itemId) {
				amount = items.get(i).getAmount();
				items.remove(i);
				break;
			}
		}
		return amount;
	}

	public void remove(int id, int amount) {
		for (int i = 0; i < items.size(); i++) {
			Item item = items.get(i);
			if (item.getId() == id) {
				int newAmount = item.getAmount() - amount;
				if (newAmount <= 0) {
					items.remove(item);
				} else {
					items.set(i, new Item(id, item.getAmount() - amount));
				}
				break;
			}
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getVarbit() {
		return varbit;
	}

	public void setVarbit(int varbit) {
		this.varbit = varbit;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public int getSlot(int itemId) {
		System.out.println("----");
		for (int i = 0; i < items.size(); i++) {
			System.out.println("Item: " + items.get(i) + " slot:" + i);
			if (items.get(i).getId() == itemId) {
				return i;
			}
		}
		return -1;
	}

}
