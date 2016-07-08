package edgeville.model;

/**
 * @author Simon on 8/22/2015.
 */
public class Area {

	private final int x1, x2, z1, z2;

	public Area(int x1, int z1, int x2, int z2) {
		this.x1 = x1;
		this.x2 = x2;
		this.z1 = z1;
		this.z2 = z2;
	}

	public Area(Tile spawnTile, int radius) {
		this(spawnTile.x - radius, spawnTile.z - radius, spawnTile.x + radius, spawnTile.z + radius);
	}

	public boolean contains(Tile t) {
		return t.x >= x1 && t.x <= x2 && t.z >= z1 && t.z <= z2;
	}

	public int x1() {
		return x1;
	}

	public int x2() {
		return x2;
	}

	public int z1() {
		return z1;
	}

	public int z2() {
		return z2;
	}

	@Override
	public String toString() {
		return "Area[" + x1 + ".." + z1 + ", " + x2 + ".." + z2 + "]";
	}
}
