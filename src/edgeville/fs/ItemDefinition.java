package edgeville.fs;

import io.netty.buffer.Unpooled;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edgeville.io.RSBuffer;

/**
 * @author Simon on 10/4/2014.
 */
public class ItemDefinition implements Definition {

	private static Map<Integer, Object> NO_PARAMS = Collections.EMPTY_MAP;

	public int resizey;
	public int xan2d;
	public int cost = 1;
	public int inventoryModel;
	public int resizez;
	public short[] recol_s;
	public short[] recol_d;
	public String name = "null";
	public int zoom2d = 2000;
	public int yan2d;
	public int zan2d;
	public int yof2d;
	private boolean stackable;
	public int[] countco;
	public boolean members = false;
	public String[] options = new String[5];
	public String[] ioptions = new String[5];
	public int maleModel0;
	public int maleModel1;
	public short[] retex_s;
	public short[] retex_d;
	public int femaleModel1;
	public int maleModel2;
	public int xof2d;
	public int manhead;
	public int manhead2;
	public int womanhead;
	public int womanhead2;
	public int[] countobj;
	public int femaleModel2;
	public int unnotedID;
	public int femaleModel0;
	public int resizex;
	public int noteModel;
	public int ambient;
	public int contrast;
	public int team;
	public boolean grandexchange;
	public boolean dummyitem;

	public int id;

	public ItemDefinition(int id, byte[] data) {
		this.id = id;

		if (data != null && data.length > 0)
			decode(new RSBuffer(Unpooled.wrappedBuffer(data)));
	}

	void decode(RSBuffer buffer) {
		while (true) {
			int op = buffer.readUByte();
			if (op == 0)
				break;
			decode(buffer, op);
		}
	}

	void decode(RSBuffer buffer, int code) {
		if (code == 1) {
			inventoryModel = buffer.readUShort();
		} else if (code == 2) {
			name = buffer.readString();
		} else if (code == 4) {
			zoom2d = buffer.readUShort();
		} else if (code == 5) {
			xan2d = buffer.readUShort();
		} else if (code == 6) {
			yan2d = buffer.readUShort();
		} else if (code == 7) {
			xof2d = buffer.readUShort();
			if (xof2d > 0x7FFF) {
				xof2d -= 0x10000;
			}
		} else if (code == 8) {
			yof2d = buffer.readUShort();
			if (yof2d > 0x7FFF) {
				yof2d -= 0x10000;
			}
		} else if (code == 11) {
			stackable = true;
		} else if (code == 12) {
			cost = buffer.readInt();
		} else if (code == 16) {
			members = true;
		} else if (code == 23) {
			maleModel0 = buffer.readUShort();
			buffer.readByte();
		} else if (code == 24) {
			maleModel1 = buffer.readUShort();
		} else if (code == 25) {
			femaleModel0 = buffer.readUShort();
			buffer.readByte();
		} else if (code == 26) {
			femaleModel1 = buffer.readUShort();
		} else if (code >= 30 && code < 35) {
			options[code - 30] = buffer.readString();
			if (options[code - 30].equalsIgnoreCase("null")) {
				options[code - 30] = null;
			}
		} else if (code >= 35 && code < 40) {
			ioptions[code - 35] = buffer.readString();
		} else if (code == 40) {
			int num = buffer.readUByte();
			recol_s = new short[num];
			recol_d = new short[num];

			for (int var4 = 0; var4 < num; ++var4) {
				recol_s[var4] = (short) buffer.readUShort();
				recol_d[var4] = (short) buffer.readUShort();
			}
		} else if (code == 41) {
			int num = buffer.readUByte();
			retex_s = new short[num];
			retex_d = new short[num];

			for (int var4 = 0; var4 < num; ++var4) {
				retex_s[var4] = (short) buffer.readUShort();
				retex_d[var4] = (short) buffer.readUShort();
			}
		} else if (code == 65) {
			grandexchange = true;
		} else if (code == 78) {
			maleModel2 = buffer.readUShort();
		} else if (code == 79) {
			femaleModel2 = buffer.readUShort();
		} else if (code == 90) {
			manhead = buffer.readUShort();
		} else if (code == 91) {
			womanhead = buffer.readUShort();
		} else if (code == 92) {
			manhead2 = buffer.readUShort();
		} else if (code == 93) {
			womanhead2 = buffer.readUShort();
		} else if (code == 95) {
			zan2d = buffer.readUShort();
		} else if (code == 96) {
			dummyitem = buffer.readByte() == 1;
		} else if (code == 97) {
			unnotedID = buffer.readUShort();
		} else if (code == 98) {
			noteModel = buffer.readUShort();
		} else if (code >= 100 && code < 110) {
			if (countobj == null) {
				countobj = new int[10];
				countco = new int[10];
			}

			countobj[code - 100] = buffer.readUShort();
			countco[code - 100] = buffer.readUShort();
		} else if (code == 110) {
			resizex = buffer.readUShort();
		} else if (code == 111) {
			resizey = buffer.readUShort();
		} else if (code == 112) {
			resizez = buffer.readUShort();
		} else if (code == 113) {
			ambient = buffer.readByte();
		} else if (code == 114) {
			contrast = buffer.readByte();
		} else if (code == 115) {
			team = buffer.readUByte();
		} else if (code == 139) {
			buffer.readShort();
		} else if (code == 140) {
			buffer.readShort();
		} else if (code == 148) {
			buffer.readShort(); // this is new
		} else {
			throw new RuntimeException("cannot parse item definition, missing config code: " + code);
		}
	}

	public boolean stackable() {
		return stackable || noteModel > 0;
	}

}
