package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.Entity;
import edgeville.model.Tile;
import edgeville.model.entity.Player;

/**
 * @author Simon on 8/18/2015.
 */
public class FireProjectile implements Command {

	private Tile from;
	private Entity to;
	private Tile toTile;
	private int gfx;
	private int startHeight;
	private int endHeight;
	private int delay;
	private int lifetime;
	private int angle;
	private int steepness;

	public FireProjectile(Tile from, Entity to, int gfx, int startHeight, int endHeight, int delay, int lifetime, int angle, int steepness) {
		this.from = from;
		this.to = to;
		this.gfx = gfx;
		this.startHeight = startHeight;
		this.endHeight = endHeight;
		this.delay = delay;
		this.lifetime = lifetime;
		this.angle = angle;
		this.steepness = steepness;

		if (this.to != null)
			toTile = to.getTile();
	}

	public FireProjectile(Tile from, Tile to, int gfx, int startHeight, int endHeight, int delay, int lifetime, int angle, int steepness) {
		this.from = from;
		this.gfx = gfx;
		this.startHeight = startHeight;
		this.endHeight = endHeight;
		this.delay = delay;
		this.lifetime = lifetime;
		this.angle = angle;
		this.steepness = steepness;
		this.toTile = to;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(16));

		buffer.packet(101);

		int tx = (toTile.x - from.x);
		int tz = (toTile.z - from.z);

		buffer.writeByte((from.x << 4) | from.z);
		buffer.writeByte(0); //tz
		buffer.writeByte(6);//ty

		if (to != null)
			buffer.writeShort(to.isNpc() ? to.index()+1 : -(to.index()+1));
		else
			buffer.writeShort(0);

		buffer.writeShort(gfx);
		buffer.writeByte(startHeight);
		buffer.writeByte(endHeight);
		buffer.writeShort(delay);
		buffer.writeShort(delay + lifetime);
		buffer.writeByte(angle);
		buffer.writeByte(steepness);

		return buffer;
	}

}
