package edgeville.net.message.game.encoders;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edgeville.io.RSBuffer;
import edgeville.model.Tile;
import edgeville.model.entity.Player;
import edgeville.util.map.MapDecryptionKeys;

/**
 * @author Simon on 8/22/2014.
 */
public class DisplayMap implements Command { // Aka dipsleemap

	private int x;
	private int z;
	private int localX;
	private int localZ;
	private int level;
	private int[][] xteaKeys;

	public DisplayMap(Player player) {
		this(player, player.getTile(), true);
	}

	public DisplayMap(Player player, Tile tile, boolean setActive) {
		int x = tile.x;
		int z = tile.z;

		int base_x = x / 8;
		int base_z = z / 8;

		int botleft_x = (base_x - 6) * 8;
		int botleft_z = (base_z - 6) * 8;

		this.x = base_x;
		this.z = base_z;
		this.localX = x - botleft_x;
		this.localZ = z - botleft_z;
		level = tile.level;

		// Update last map
		if (setActive) {
			player.activeMap(new Tile(botleft_x, botleft_z));
		}
	}

	private void initGPI(RSBuffer buf, Player player) {
		if (player.initialized)
			return;
		System.out.println("GPI initialized");
		player.world().registerPlayer(player);
		buf.startBitMode();
		buf.writeBits(30, player.getTile().toPacked());
		
		for (int playerIndex = 1; playerIndex < 2048; playerIndex++) {
			if (playerIndex != player.index()) {
				buf.writeBits(18, player.getTile().toRegionPacked());
			}
		}
		
		buf.endBitMode();
		player.initialized = true;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buf = new RSBuffer(player.channel().alloc().buffer(12 + 4 * 4 * 9));
		buf.packet(42).writeSize(RSBuffer.SizeType.SHORT);

		initGPI(buf, player);

		System.out.println("sending x: " + x);
		buf.writeLEShortA(x); // region x
		System.out.println("sending z: " + z);
		buf.writeShortA(z); // region y

		boolean forceSend = true;

		if ((x / 8 == 48 || x / 8 == 49) && z / 8 == 48)
			forceSend = false;

		if (x / 8 == 48 && z / 8 == 148)
			forceSend = false;

		int xteaCount = 0;
		List<Integer> xteas = new ArrayList<Integer>();
		for (int xCalc = (x - 6) / 8; xCalc <= ((x + 6) / 8); xCalc++) {
			for (int yCalc = (z - 6) / 8; yCalc <= ((z + 6) / 8); yCalc++) {
				int region = (xCalc << 8) + yCalc;
				System.out.println("region xteas:" + region);
				
				
				int[] xtea = MapDecryptionKeys.get(region);
				if (forceSend || ((yCalc != 49) && (yCalc != 149) && (yCalc != 147) && (xCalc != 50)
						&& ((xCalc != 49) || (yCalc != 47)))) {
					if (xtea != null) {
						xteas.add(xtea[0]);
						xteas.add(xtea[1]);
						xteas.add(xtea[2]);
						xteas.add(xtea[3]);
					} else {
						xteas.add(0);
						xteas.add(0);
						xteas.add(0);
						xteas.add(0);
					}

					xteaCount++;
				}
			}
		}

		System.out.println("count:" + xteaCount);
		buf.writeShort(xteaCount); // calculate region count in loop
		for (int xtea : xteas) {
			buf.writeInt(xtea); // calculate region count in loop
		}
		return buf;
	}
}
