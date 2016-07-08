package edgeville.fs;

import edgeville.io.RSBuffer;
import io.netty.buffer.Unpooled;

/**
 * @author Simon on 10/4/2014.
 */
public class ObjectDefinition implements Definition {

	public String name = "null";
	public int[] modeltypes;
	public int[] models;
	public int sizeX = 1;
	public int sizeY = 1;
	public int clipType = 2;
	public boolean projectileClipped = true;
	public int anInt2292 = -1;
	public int anInt2296 = -1;
	public boolean aBool2279 = false;
	public boolean aBool2280 = false;
	public int anInt2281 = -1;
	public int anInt2291 = -1;
	public int anInt2283 = 0;
	public int anInt2285 = 0;
	public short[] recol_s;
	public short[] recol_d;
	public short[] retex_s;
	public short[] retex_d;
	public int anInt2286 = -1;
	public boolean aBool2288 = false;
	public boolean aBool2284 = true;
	public int anInt2259 = -1;
	public int anInt2297 = -1;
	public int anInt2293 = -1;
	public int anInt2287 = -1;
	public int anInt2307 = 0;
	public int anInt2294 = 0;
	public int anInt2295 = 0;
	public boolean aBool2264 = false;
	public boolean unclipped = false;
	public int anInt2298 = -1;
	public int varbit = -1;
	public int anInt2302 = -1;
	public int anInt2303 = 0;
	public int varp = -1;
	public int anInt2304 = 0;
	public int anInt2290 = 0;
	public int cflag = 0;
	public int[] anIntArray2306;
	public int[] to_objs;
	public String[] options = new String[5];

	public int id;

	public ObjectDefinition(int id, byte[] data) {
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
			int count = buffer.readUByte();
			if (count > 0) {
				modeltypes = new int[count];
				models = new int[count];

				for (int i = 0; i < count; i++) {
					models[i] = buffer.readUShort();
					modeltypes[i] = buffer.readUByte();
				}
			}
		} else if (code == 2) {
			name = buffer.readString();
		} else if (code == 5) {
			int num = buffer.readUByte();
			if (num > 0) {
				modeltypes = null;
				models = new int[num];

				for (int i = 0; i < num; i++) {
					models[i] = buffer.readUShort();
				}
			}
		} else if (code == 14) {
			sizeX = buffer.readUByte();
		} else if (code == 15) {
			sizeY = buffer.readUByte();
		} else if (code == 17) {
			clipType = 0;
			projectileClipped = false;
		} else if (code == 18) {
			projectileClipped = false;
		} else if (code == 19) {
			anInt2292 = buffer.readUByte();
		} else if (code == 21) {
			anInt2296 = 0;
		} else if (code == 22) {
			aBool2279 = true;
		} else if (code == 23) {
			aBool2280 = true;
		} else if (code == 24) {
			anInt2281 = buffer.readUShort();
			if (anInt2281 == 65535) {
				anInt2281 = -1;
			}
		} else if (code == 27) {
			clipType = 1;
		} else if (code == 28) {
			anInt2291 = buffer.readUByte();
		} else if (code == 29) {
			anInt2283 = buffer.readByte();
		} else if (code == 39) {
			anInt2285 = buffer.readByte();
		} else if (code >= 30 && code < 35) {
			options[code - 30] = buffer.readString();
			if (options[code - 30].equalsIgnoreCase("null")) {
				options[code - 30] = null;
			}
		} else if (code == 40) {
			int count = buffer.readUByte();
			recol_s = new short[count];
			recol_d = new short[count];

			for (int i = 0; i < count; i++) {
				recol_s[i] = (short) buffer.readUShort();
				recol_d[i] = (short) buffer.readUShort();
			}
		} else if (code == 41) {
			int count = buffer.readUByte();
			retex_s = new short[count];
			retex_d = new short[count];

			for (int i = 0; i < count; i++) {
				retex_s[i] = (short) buffer.readUShort();
				retex_d[i] = (short) buffer.readUShort();
			}
		} else if (code == 60) {
			anInt2286 = buffer.readUShort();
		} else if (code == 62) {
			aBool2288 = true;
		} else if (code == 64) {
			aBool2284 = false;
		} else if (code == 65) {
			anInt2259 = buffer.readUShort();
		} else if (code == 66) {
			anInt2297 = buffer.readUShort();
		} else if (code == 67) {
			anInt2293 = buffer.readUShort();
		} else if (code == 68) {
			anInt2287 = buffer.readUShort();
		} else if (code == 69) {
			cflag = buffer.readUByte(); // clip access flag
		} else if (code == 70) {
			anInt2307 = buffer.readShort();
		} else if (code == 71) {
			anInt2294 = buffer.readShort();
		} else if (code == 72) {
			anInt2295 = buffer.readShort();
		} else if (code == 73) {
			aBool2264 = true;
		} else if (code == 74) {
			unclipped = true;
		} else if (code == 75) {
			anInt2298 = buffer.readUByte();
		} else if (code == 77) {
			varbit = buffer.readUShort();
			if (varbit == 65535) {
				varbit = -1;
			}

			varp = buffer.readUShort();
			if (varp == 65535) {
				varp = -1;
			}

			int count = buffer.readUByte();
			to_objs = new int[1 + count];

			for (int i = 0; i <= count; i++) {
				to_objs[i] = buffer.readUShort();
				if (to_objs[i] == 65535) {
					to_objs[i] = -1;
				}
			}
		} else if (code == 78) {
			anInt2302 = buffer.readUShort();
			anInt2303 = buffer.readUByte();
		} else if (code == 79) {
			anInt2304 = buffer.readUShort();
			anInt2290 = buffer.readUShort();
			anInt2303 = buffer.readUByte();
			int count = buffer.readUByte();
			anIntArray2306 = new int[count];

			for (int i = 0; i < count; i++) {
				anIntArray2306[i] = buffer.readUShort();
			}
		} else if (code == 81) {
			anInt2296 = buffer.readUByte();
		} else {
			throw new RuntimeException("cannot parse npc definition, missing config code: " + code);
		}
	}

}
