package edgeville.model.map;

import edgeville.model.Entity;
import edgeville.model.Tile;
import edgeville.model.entity.Npc;

public class EntityStrategy extends RouteStrategy {

	/**
	 * Entity position x.
	 */
	private int x;
	/**
	 * Entity position y.
	 */
	private int z;
	/**
	 * Entity size.
	 */
	private int size;
	/**
	 * Access block flag, see RouteStrategy static final values.
	 */
	private int accessBlockFlag;

	private boolean combat = false;

	public EntityStrategy(Entity entity) {
		this(entity, 0);
	}

	public EntityStrategy(Tile tile) {
		size = 1;
		accessBlockFlag = 0;
		x = tile.x;
		z = tile.z;
	}

	public EntityStrategy(Entity entity, int accessBlockFlag) {
		x = entity.getTile().x;
		z = entity.getTile().z;
		//size = 1;
		size = (entity instanceof Npc ? ((Npc) entity).def().size : 1);
		this.accessBlockFlag = accessBlockFlag;
	}

	public EntityStrategy(Entity entity, int accessBlockFlag, Tile t) {
		x = t.x;
		z = t.z;
		//size = 1;
		size = (entity instanceof Npc ? ((Npc) entity).def().size : 1);
		this.accessBlockFlag = accessBlockFlag;
	}

	public EntityStrategy(Entity entity, int accessBlockFlag, boolean combat) {
		x = entity.getTile().x;
		z = entity.getTile().z;
		//size = 1;
		size = (entity instanceof Npc ? ((Npc) entity).def().size : 1);
		this.accessBlockFlag = accessBlockFlag;
		this.combat = combat;
	}

	public EntityStrategy(Entity entity, int accessBlockFlag, boolean combat, Tile t) {
		x = t.x;
		z = t.z;
		//size = 1;
		size = (entity instanceof Npc ? ((Npc) entity).def().size : 1);
		this.accessBlockFlag = accessBlockFlag;
		this.combat = combat;
	}

	public EntityStrategy combat() {
		combat = true;
		return this;
	}

	@Override
	public boolean canExit(int currentX, int currentY, int sizeXY, int[][] clip, int clipBaseX, int clipBaseY) {
		if (combat)
			checkFilledRectangularInteractCombat(clip, currentX - clipBaseX, currentY - clipBaseY, sizeXY, sizeXY, x - clipBaseX, z - clipBaseY, size, size, accessBlockFlag);
		return checkFilledRectangularInteract(clip, currentX - clipBaseX, currentY - clipBaseY, sizeXY, sizeXY, x - clipBaseX, z - clipBaseY, size, size, accessBlockFlag);
	}

	@Override
	public int getApproxDestinationX() {
		return x;
	}

	@Override
	public int getApproxDestinationZ() {
		return z;
	}

	@Override
	public int getApproxDestinationSizeX() {
		return size;
	}

	@Override
	public int getApproxDestinationSizeZ() {
		return size;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof EntityStrategy))
			return false;
		EntityStrategy strategy = (EntityStrategy) other;
		return x == strategy.x && z == strategy.z && size == strategy.size && accessBlockFlag == strategy.accessBlockFlag;
	}

}
