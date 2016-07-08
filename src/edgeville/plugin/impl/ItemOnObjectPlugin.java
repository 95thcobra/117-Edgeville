package edgeville.plugin.impl;

import edgeville.model.Tile;
import edgeville.model.item.Item;
import edgeville.plugin.PluginContext;

/**
 * The plugin context for the item on object message.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class ItemOnObjectPlugin implements PluginContext {

    /**
     * The identifier for the object that was clicked.
     */
    private final int id;

    /**
     * The position of the object that was clicked.
     */
    private final Tile position;

    /**
     * The size of the object that was clicked.
     */
    private final int size;

    /**
     * The item that was used with the object.
     */
    private final Item item;

    /**
     * The slot of the item that was used with the object.
     */
    private final int slot;

    /**
     * Create a new {@link ItemOnObjectPlugin}.
     *
     * @param id       the identifier for the object that was clicked.
     * @param position the position of the object that was clicked.
     * @param size     the size of the object that was clicked.
     * @param item     the item that was used with the object.
     * @param slot     the slot of the item that was used with the object.
     */
    public ItemOnObjectPlugin(int id, Tile position, int size, Item item, int slot) {
        this.id = id;
        this.position = position;
        this.size = size;
        this.item = item;
        this.slot = slot;
    }

    /**
     * Gets the identifier for the object that was clicked.
     *
     * @return the object identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the position of the object that was clicked.
     *
     * @return the position of the object.
     */
    public Tile getPosition() {
        return position;
    }

    /**
     * Gets the size of the object that was clicked.
     *
     * @return the size of the object.
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the item that was used with the object.
     *
     * @return the item that was used.
     */
    public Item getItem() {
        return item;
    }

    /**
     * Gets the slot of the item that was used with the object.
     *
     * @return the slot of the item that was used.
     */
    public int getSlot() {
        return slot;
    }
}
