package edgeville.fs;

import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.Map;

import edgeville.io.RSBuffer;

/**
 * @author Simon on 7/20/15.
 */
public class EnumDefinition implements Definition {

	public int id;

	private int keytype;
	private int valuetype;
	private int defaultInt;
	private String defaultString;

	private Map<Integer, Object> enums = new HashMap<>();

	public EnumDefinition(int id, byte[] data) {
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
			keytype = buffer.readByte();
		} else if (code == 2) {
			valuetype = buffer.readByte();
		} else if (code == 3) {
			defaultString = buffer.readString();
		} else if (code == 4) {
			defaultInt = buffer.readInt();
		} else if (code == 5 || code == 6) {
			int count = buffer.readUShort();

			for (int i = 0; i < count; i++) {
				int key = buffer.readInt();

				if (code == 5) {
					enums.put(key, buffer.readString());
				} else {
					enums.put(key, buffer.readInt());
				}
			}
		} else {
			throw new RuntimeException("unrecognized enum code " + code);
		}
	}

	public int keyType() {
		return keytype;
	}

	public int valueType() {
		return valuetype;
	}

	public int defaultInt() {
		return defaultInt;
	}

	public String defaultString() {
		return defaultString;
	}

	public int getInt(int key) {
		return (int) enums.getOrDefault(key, defaultInt);
	}

	public String getString(int key) {
		return enums.getOrDefault(key, defaultString).toString();
	}

	public Map<Integer, Object> enums() {
		return enums;
	}

}
