package edgeville.net.message.game.encoders;

import java.util.List;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.model.item.Item;
import edgeville.model.item.ItemContainer;

/**
 * @author Simon on 8/22/2014.
 */
public class SetItems implements Command {

	private int target;
	private int targetChild;
	private int key;
	private Item[] container;

	public SetItems(int target, int targetChild, ItemContainer container) {
		this.target = target;
		this.targetChild = targetChild;
		this.container = container.copy();
	}

	public SetItems(int key, int target, int targetChild, ItemContainer container) {
		this.key = key;
		this.target = target;
		this.targetChild = targetChild;
		this.container = container.copy();
	}

	public SetItems(int key, ItemContainer container) {
		this.key = key;
		this.container = container.copy();
	}

	public SetItems(int key, List<Item> items/*, int length*/) {
		this.key = key;
		this.items = items;
		//this.length = length;
	}

	private List<Item> items;
	//private int length;

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(8));

		buffer.packet(235).writeSize(RSBuffer.SizeType.SHORT);

		buffer.writeInt(target == 0 ? -1 : ((target << 16) | targetChild));
		buffer.writeShort(key);

		//////////////////// custom by sj
		if (items != null) {
			buffer.writeShort(items.size());

			for (Item item : items) {
				if (item == null) {
					buffer.writeShort(0);
					buffer.writeByte(0);
				} else {
					buffer.writeShort(item.getId() + 1);
					buffer.writeByte(Math.min(255, item.getAmount()));

					if (item.getAmount() >= 255)
						buffer.writeLEInt(item.getAmount());
				}
			}
			return buffer;
		}
		//////////////////////

		buffer.writeShort(container.length);

		for (Item item : container) {
			if (item == null) {
				buffer.writeShort(0);
				buffer.writeByte(0);
			} else {
				buffer.writeShort(item.getId() + 1);
				buffer.writeByte(Math.min(255, item.getAmount()));

				if (item.getAmount() >= 255)
					buffer.writeLEInt(item.getAmount());
			}
		}

		return buffer;
	}
}
