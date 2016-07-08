package edgeville.model.entity.player;

import edgeville.model.entity.Player;
import edgeville.model.item.Item;
import edgeville.net.message.game.encoders.InterfaceText;
import edgeville.util.EquipmentInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Simon on 8/23/2014.
 */
public class Looks {

	private byte[] calculated;

	private Player player;
	private int transmog = -1;
	private ByteBuf calcBuffer;
	private int[] renderpairOverride;

	public Looks(Player player) {
		this.player = player;
		calcBuffer = Unpooled.buffer(128);
	}

	public void update() {
		calcBuffer.readerIndex(0);
		calcBuffer.writerIndex(0); // Start at 0

		calcBuffer.writeByte(0); // Gender
		calcBuffer.writeByte(player.getSkullHeadIcon()); // Skull
		calcBuffer.writeByte(player.getPrayerHeadIcon()); // Prayer

		if (transmog >= 0) {
			calcBuffer.writeShort(0xFFFF).writeShort(transmog);
		} else {
			int[] looks = {0, 0, 0, 0, 18, 0, 26/*arms*/, 36, 7, 33, 42, 10/*beard*/};
			//6 = arms
			//8 = hair
			//9 = hands
			//10 = boots
			//11 = beard
			EquipmentInfo equipInfo = player.world().equipmentInfo();
			for (int i = 0; i < 12; i++) {
				if (i == 6 && player.getEquipment().hasAt(4) && equipInfo.typeFor(player.getEquipment().get(4).getId()) == 6) {
					calcBuffer.writeByte(0);
					continue;
				}
				
				if (i == 8 && player.getEquipment().hasAt(0) && equipInfo.typeFor(player.getEquipment().get(0).getId()) == 8) {
					calcBuffer.writeByte(0);
					continue;
				}
				
				if (i == 11 && player.getEquipment().hasAt(0) && equipInfo.typeFor(player.getEquipment().get(0).getId()) == 8) {

				}

				if (player.getEquipment().hasAt(i)) {			
					calcBuffer.writeShort(0x200 + player.getEquipment().get(i).getId());
				} else if (looks[i] != 0) {
					calcBuffer.writeShort(0x100 + looks[i]);
				} else {
					calcBuffer.writeByte(0);
				}
			}
		}

		// Dem colors
		calcBuffer.writeByte(3).writeByte(16).writeByte(16).writeByte(0).writeByte(0);

		int weapon = player.getEquipment().hasAt(EquipSlot.WEAPON) ? player.getEquipment().get(EquipSlot.WEAPON).getId() : -1;
		int[] renderpair = renderpairOverride != null ? renderpairOverride : player.world().equipmentInfo().renderPair(weapon);
		for (int renderAnim : renderpair)
			calcBuffer.writeShort(renderAnim); // Renderanim

		/* Str idgaf */
		calcBuffer.writeBytes(player.name().getBytes()).writeByte(0);//with terminator 0

		calcBuffer.writeByte(player.skills().combatLevel());
		calcBuffer.writeShort(0);
		calcBuffer.writeByte(0);

		calculated = new byte[calcBuffer.writerIndex()];
		calcBuffer.readerIndex(0);
		calcBuffer.readBytes(calculated);

		player.sync().calculateLooks();
	}

	public void transmog(int id) {
		transmog = id;
		update();
	}

	public void render(int... pair) {
		renderpairOverride = pair;
		update();
	}

	public void resetRender() {
		renderpairOverride = null;
		update();
	}

	public byte[] get() {
		/* Just in case... */
		if (calculated == null)
			update();

		return calculated;
	}

	private static int[] renderFor(int id) {
		return new int[] {};
	}

}
