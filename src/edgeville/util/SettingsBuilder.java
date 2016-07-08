package edgeville.util;

/**
 * @author Simon on 8/11/2015.
 */
public class SettingsBuilder {

	private int value;

	public SettingsBuilder option(int opt) {
		value |= 1 << opt + 1;
		return this;
	}

	public int build() {
		return value;
	}

}
