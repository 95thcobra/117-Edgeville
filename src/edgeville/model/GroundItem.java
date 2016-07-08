package edgeville.model;

import edgeville.model.entity.Player;
import edgeville.model.item.Item;

/**
 * @author Simon on 8/22/2015.
 */
public class GroundItem {

	private final Item item;
	private final Tile tile;
	private final Player owner;
	private final long spawned = System.currentTimeMillis();
	private boolean broadcasted = false;

	public GroundItem(Item item, Tile tile, Player owner) {
		this.item = item;
		this.tile = tile;
		this.owner = owner;

		if (owner == null)
			broadcasted = true;
	}

	public GroundItem(Item item, Tile tile) {
		this(item, tile, null);
	}

	public Item item() {
		return item;
	}

	public Player getOwner() {
		return owner;
	}

	public Tile tile() {
		return tile;
	}

	public boolean broadcasted() {
		return broadcasted;
	}

	public void broadcasted(boolean b) {
		broadcasted = b;
	}

	public boolean shouldBroadcast() {
		return System.currentTimeMillis() >= spawned + 60_000;
	}

	public boolean shouldBeRemoved() {
		return System.currentTimeMillis() >= spawned + 120_000;
	}

}
