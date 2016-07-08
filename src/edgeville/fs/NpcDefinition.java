package edgeville.fs;

import edgeville.io.RSBuffer;
import io.netty.buffer.Unpooled;

/**
 * @author Simon on 10/4/2014.
 */
public class NpcDefinition implements Definition {

	int[] models;
	public String name = "null";
	public int size = 1;
	public int anInt2225 = -1;
	public int renderAnim = -1;
	public int anInt2233 = -1;
	public int anInt2238 = -1;
	public int anInt2229 = -1;
	public int anInt2230 = -1;
	public int anInt2231 = -1;
	short[] recol_s;
	short[] recol_d;
	short[] retex_s;
	short[] retex_d;
	int[] anIntArray2224;
	public boolean mapdot = true;
	public int combatlevel = -1;
	int anInt2232 = -1;
	int anInt2216 = -1;
	public boolean aBool2219 = false;
	int anInt2242 = 0;
	int anInt2243 = 0;
	public int anInt2244 = -1;
	public int anInt2223 = -1;
	int anInt2247 = -1;
	public boolean aBool2249 = true;
	int anInt2226 = -1;
	public boolean aBool2227 = true;
	public int[] anIntArray2246;
	public boolean aBool2251 = false;
	public int anInt2252 = -1;
	public String[] options = new String[5];

	public int id;

	public NpcDefinition(int id, byte[] data) {
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
			int numModels = buffer.readUByte();
			models = new int[numModels];

			for (int mdl = 0; mdl < numModels; mdl++) {
				models[mdl] = buffer.readUShort();
			}
		} else if (code == 2) {
			name = buffer.readString();
        } else if (code == 12) {
			size = buffer.readUByte();
		} else if (code == 13) {
			anInt2225 = buffer.readUShort();
		} else if (code == 14) {
			renderAnim = buffer.readUShort();
		} else if (code == 15) {
			anInt2233 = buffer.readUShort();
		} else if (code == 16) {
			anInt2238 = buffer.readUShort();
		} else if (code == 17) {
			renderAnim = buffer.readUShort();
			anInt2229 = buffer.readUShort();
			anInt2230 = buffer.readUShort();
			anInt2231 = buffer.readUShort();
		} else if (code >= 30 && code < 35) {
			options[code - 30] = buffer.readString();
			if (options[code - 30].equalsIgnoreCase("null")) {
				options[code - 30] = null;
			}
		} else if (code == 40) {
			int var5 = buffer.readUByte();
			recol_s = new short[var5];
			recol_d = new short[var5];

			for (int var4 = 0; var4 < var5; var4++) {
				recol_s[var4] = (short) buffer.readUShort();
				recol_d[var4] = (short) buffer.readUShort();
			}
		} else if (code == 41) {
			int var5 = buffer.readUByte();
			retex_s = new short[var5];
			retex_d = new short[var5];

			for (int var4 = 0; var4 < var5; var4++) {
				retex_s[var4] = (short) buffer.readUShort();
				retex_d[var4] = (short) buffer.readUShort();
			}
		} else if (code == 60) {
			int var5 = buffer.readUByte();
			anIntArray2224 = new int[var5];

			for (int var4 = 0; var4 < var5; var4++) {
				anIntArray2224[var4] = buffer.readUShort();
			}
		} else if (code == 93) {
			mapdot = false;
		} else if (code == 95) {
			combatlevel = buffer.readUShort();
		} else if (code == 97) {
			anInt2232 = buffer.readUShort();
		} else if (code == 98) {
			anInt2216 = buffer.readUShort();
		} else if (code == 99) {
			aBool2219 = true;
		} else if (code == 100) {
			anInt2242 = buffer.readByte();
		} else if (code == 101) {
			anInt2243 = buffer.readByte();
		} else if (code == 102) {
			anInt2244 = buffer.readUShort();
		} else if (code == 103) {
			anInt2223 = buffer.readUShort();
		} else if (code == 106) {
			anInt2247 = buffer.readUShort();
			if (anInt2247 == 65535) {
				anInt2247 = -1;
			}

			anInt2226 = buffer.readUShort();
			if (anInt2226 == 65535) {
				anInt2226 = -1;
			}

			int var5 = buffer.readUByte();
			anIntArray2246 = new int[var5 + 1];

			for (int var4 = 0; var4 <= var5; var4++) {
				anIntArray2246[var4] = buffer.readUShort();
				if (anIntArray2246[var4] == 65535) {
					anIntArray2246[var4] = -1;
				}
			}
		} else if (code == 107) {
			aBool2249 = false;
		} else if (code == 109) {
			aBool2227 = false;
		} else if (code == 111) {
			aBool2251 = true;
		} else if (code == 112) {
			anInt2252 = buffer.readUByte();
		} else {
			throw new RuntimeException("cannot parse npc definition, missing config code: " + code);
		}
	}

}
