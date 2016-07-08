package edgeville.model.item;

import java.util.HashMap;
import java.util.Map;

import edgeville.fs.ItemDefinition;
import edgeville.model.World;

/**
 * @author Simon on 8/31/2014.
 */
public final class Item {

	private final int id;
	private final int amount;
	private Map<String, Object> properties;

	public Item(Item item) {
		this(item, item.getAmount());
	}

	public Item(Item item, int amount) {
		id = item.id;
		this.amount = amount;

		if (item.hasProperties())
			properties = new HashMap<>(item.properties);
	}

	public Item(int id) {
		this.id = id;
		amount = 1;
	}

	public Item(int id, int amount) {
		this.id = id;
		this.amount = amount;
	}

	public int getId() {
		return id;
	}

	public int getAmount() {
		return amount;
	}

	public boolean hasProperties() {
		return properties != null && properties.size() > 0;
	}

	public void property(String key, Object value) {
		if (properties == null)
			properties = new HashMap<>();
		properties.put(key, value);
	}

	public Object property(String key) {
		return hasProperties() ? properties.get(key) : null;
	}

	/**
	 * Resolve this item's definition in the world's repository for definitions. No definition returns <code>null</code>.
	 * @param world The world to use to resolve the definition for this item.
	 * @return The item's definitions, or <code>null</code> if that didn't work out.
	 */
	public ItemDefinition definition(World world) {
		return world.definitions().get(ItemDefinition.class, id);
	}
	
	public String toString() {
		return String.format("Item id: %d, amount: %d", id, amount);
	}

}
