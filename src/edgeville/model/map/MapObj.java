package edgeville.model.map;

import edgeville.fs.ObjectDefinition;
import edgeville.model.Tile;
import edgeville.model.World;

/**
 * @author Simon on 8/23/2015.
 */
public class MapObj {

	private final Tile tile;
	private final int id;
	private byte type;
	private byte rot;

	public MapObj( Tile tile, int id, int type, int rot) {
		this.id = id;
		this.tile = tile;
		this.type = (byte) type;
		this.rot = (byte) rot;
	}

	public ObjectDefinition definition(World world) {
		return world.definitions().get(ObjectDefinition.class, id);
	}

	public Tile tile() {
		return tile;
	}

	public int id() {
		return id;
	}

	public int type() {
		return type;
	}

	public int rot() {
		return rot;
	}

}
