package edgeville.model.entity;

import edgeville.io.RSBuffer;
import edgeville.model.ChatMessage;
import edgeville.model.Entity;
import edgeville.model.Tile;
import edgeville.model.entity.Player;
import io.netty.buffer.Unpooled;

/**
 * @author Simon on 8/23/2014.
 */
public abstract class SyncInfo {

	protected byte[] animationSet = new byte[3];
	protected byte[] graphicSet = new byte[6];
	protected byte[] shoutSet;
    public byte[] shoutSet() {
        return shoutSet;
    }
	protected byte[] faceEntitySet = new byte[2];
	protected byte[] hitSet = new byte[5];
	protected byte[] hitSet2 = new byte[5];
	protected byte[] hitSetNPC = new byte[6];
	protected byte[] hitSet2NPC = new byte[6];
	protected byte[] facetile = new byte[4];
	protected byte[] forcemove = new byte[9];

	protected int calculatedFlag;
	protected int primaryStep = -1;
	protected int secondaryStep = -1;
	protected boolean teleported = true;

	protected Entity entity;

	public SyncInfo(Entity entity) {
		this.entity = entity;
	}

	public boolean dirty() {
		return calculatedFlag != 0 || primaryStep != -1 || teleported;
	}

	public void addFlag(int flag) {
		calculatedFlag |= flag;
	}

	public int calculatedFlag() {
		return calculatedFlag;
	}

	public boolean hasFlag(int flag) {
		return (calculatedFlag & flag) != 0;
	}

	public void step(int primary, int secondary) {
		primaryStep = primary;
		secondaryStep = secondary;
	}

	public int primaryStep() {
		return primaryStep;
	}

	public int secondaryStep() {
		return secondaryStep;
	}

	public void teleported(boolean b) {
		teleported = b;
	}

	public boolean teleported() {
		return teleported;
	}
	
	public abstract void shout(String text);

	public abstract void animation(int id, int delay);

	public abstract void graphic(int id, int height, int delay);

	public abstract void faceEntity(Entity e);

	public abstract void hit(int type, int value);

	public abstract void facetile(Tile tile);

	public byte[] animationSet() {
		return animationSet;
	}

	public byte[] graphicSet() {
		return graphicSet;
	}

	public byte[] faceEntitySet() {
		return faceEntitySet;
	}

	public byte[] hitSet() {
		return hitSet;
	}

	public byte[] hitSet2() {
		return hitSet2;
	}

	public byte[] getHitSetNPC() {
		return hitSetNPC;
	}

	public byte[] getHitSet2NPC() {
		return hitSet2NPC;
	}

	public byte[] faceTileSet() {
		return facetile;
	}

	public byte[] forceMoveSet() {
		return forcemove;
	}

	public void clear() {
		calculatedFlag = 0;
		primaryStep = -1;
		secondaryStep = -1;
		teleported = false;
	}

	public void clearMovement() {
		primaryStep = secondaryStep = -1;
	}

}
