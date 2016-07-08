package edgeville.model.entity;

import edgeville.aquickaccess.events.NpcDeathEvent;
import edgeville.fs.NpcDefinition;
import edgeville.model.*;
import edgeville.model.entity.player.NpcSyncInfo;
import edgeville.net.message.game.encoders.PlaySound;

/**
 * @author Simon on 8/10/2015.
 */
public class Npc extends Entity {

	private int id;
	private Tile spawnTile;
	private int walkRadius;
	private int spawnDirection;
	private boolean inViewport = true;
	private NpcDefinition def;
	private int hp;
	private boolean respawn;

	public Npc(int id, World world, Tile tile, boolean respawn) {
		super(world, tile);
		this.id = id;
		sync = new NpcSyncInfo(this);
		spawnTile = tile;
		hp = 100;
		def = world.definitions().get(NpcDefinition.class, id);
		this.respawn = respawn;
	}

	public void inViewport(boolean b) {
		inViewport = b;
	}

	public boolean inViewport() {
		return inViewport;
	}

	public void walkRadius(int r) {
		walkRadius = r;
	}

	public int walkRadius() {
		return walkRadius;
	}

	public void spawnDirection(int d) {
		spawnDirection = d;
	}

	public int spawnDirection() {
		return spawnDirection;
	}

	public int id() {
		return id;
	}

	public NpcSyncInfo sync() {
		return (NpcSyncInfo) sync;
	}

	public NpcDefinition def() {
		return def;
	}

	@Override
	public void cycle() {
		super.cycle();

		long lastAttackTime = (System.currentTimeMillis() - (long) attribute(AttributeKey.LAST_DAMAGE, (long) 0));
		if (lastAttackTime < 10_000 && !locked() && inViewport && walkRadius > 0 && pathQueue.empty() && world.random(9) == 0) {
			int rndX = world.random(walkRadius * 2 + 1) - walkRadius;
			int rndZ = world.random(walkRadius * 2 + 1) - walkRadius;
			walkTo(spawnTile.transform(rndX, rndZ, 0), PathQueue.StepType.REGULAR);

			// Make sure we don't walk too many tiles!
			pathQueue.trimToSize(4);
			pathQueue.removeOutside(new Area(spawnTile, walkRadius));
		}
	}

	@Override
	public int hp() {
		return hp;
	}

	@Override
	public int maxHp() {
		return 100;
	}
	
	public void setHp(int hp) {
		setHp(hp, 0);
	}

	@Override
	public void setHp(int hp, int exceed) {
		this.hp = Math.min(maxHp() + exceed, hp);
	}

	@Override
	public int size() {
		return def.size;
	}

	@Override
	public boolean isPlayer() {
		return false;
	}

	@Override
	public boolean isNpc() {
		return true;
	}

	@Override
	protected void die() {
		world.getEventHandler().addEvent(this, new NpcDeathEvent(this, respawn));
	}

	@Override
	public int getAttackSound() {
		int soundId = -1;
		soundId = 24;

		switch (id) {
		case 2005:
			soundId = 400;
			break;
		}

		return soundId;
	}

	@Override
	public int getBlockSound() {
		int soundId = -1;
		switch (id) {

		// Lesser demon
		case 2005:
			soundId = 404;
			break;
		}
		return soundId;
	}

	@Override
	public int getBlockAnim() {
		int animationId = 424;

		switch (id) {

		// Lesser demon
		case 2005:
			animationId = 404;
		}

		return animationId;
	}
}
