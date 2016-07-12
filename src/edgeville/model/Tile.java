package edgeville.model;

import com.google.common.base.MoreObjects;

/**
 * @author Simon
 */
public class Tile {

	public final int x;
	public final int z;
	public final int level;

	public Tile(Tile t) {
		x = t.x;
		z = t.z;
		level = t.level;
	}

	public Tile(int x, int z) {
		this.x = x;
		this.z = z;
		this.level = 0;
	}

	public Tile(int x, int z, int level) {
		this.x = x;
		this.z = z;
		this.level = level;
	}

	public int distance(Tile tile) {
		int dx = tile.x - x;
		int dz = tile.z - z;
		return (int) Math.sqrt(dx * dx + dz * dz);
	}

	public int region() {
		return ((x >> 6) << 8) | (z >> 6);
	}

	public static int coordsToRegion(int x, int z) {
		return ((x >> 6) << 8) | (z >> 6);
	}

	public Tile transform(int dx, int dz, int dh) {
		return new Tile(x + dx, z + dz, level + dh);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Tile))
			return false;
		Tile o = (Tile) obj;
		return o.x == x && o.z == z && o.level == level;
	}

	public boolean equals(int x, int z, int level) {
		return this.x == x && this.z == z && this.level == level;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("x", x).add("z", z).add("level", level).toString();
	}

	public int toPacked() {
		return level << 30 | (z & 0x7FFF) << 15 | x & 0x7FFF;
	}
	
	public int MatrixgetTileHash() {
		return z + (x << 14) + (level << 28);
	}
	
	public int getRegionX() {
		return (x >> 6);
	}
	
	public int getXInRegion() {
		return x & 0x3F;
	}

	public int getYInRegion() {
		return z & 0x3F;
	}

	public int getRegionY() {
		return (z >> 6);
	}

	public int toRegionPacked() {
		return getRegionY() + (getRegionX() << 8) + (level << 16);
	}
}
