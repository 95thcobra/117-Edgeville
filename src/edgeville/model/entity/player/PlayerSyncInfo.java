package edgeville.model.entity.player;

import edgeville.io.RSBuffer;
import edgeville.model.ChatMessage;
import edgeville.model.Entity;
import edgeville.model.ForceMovement;
import edgeville.model.Tile;
import edgeville.model.entity.Player;
import edgeville.model.entity.SyncInfo;
import edgeville.model.entity.player.NpcSyncInfo.Flag;
import io.netty.buffer.Unpooled;

/**
 * @author Simon on 8/23/2014.
 */
public class PlayerSyncInfo extends SyncInfo {

	private byte[] looksBlock;
	private byte[] publicChatBlock;

	/* Related to player updating below */
	private int[] localPlayerIndices = new int[255];
	private int localPlayerPtr;
	private int[] removedPlayerIndices = new int[255];
	private int[] playerUpdateRequests = new int[255];
	private int playerUpdateReqPtr;
	private int[] newlyAdded = new int[100];
	private int newlyAddedPtr;

	/* Related to npc updating below */
	private int[] localNpcIndices = new int[255];
	private int localNpcPtr;
	private int[] removedNpcIndices = new int[255];
	private int[] npcUpdateRequests = new int[255];
	private int npcUpdateReqPtr;

	public PlayerSyncInfo(Player player) {
		super(player);
	}

	public int[] localPlayerIndices() {
		return localPlayerIndices;
	}

	public int[] localNpcIndices() {
		return localNpcIndices;
	}

	public int localPlayerPtr() {
		return localPlayerPtr;
	}

	public int localNpcPtr() {
		return localNpcPtr;
	}

	public void localPlayerPtr(int i) {
		localPlayerPtr = i;
	}

	public void localNpcPtr(int i) {
		localNpcPtr = i;
	}

	public int[] removedPlayerIndices() {
		return removedPlayerIndices;
	}

	public int[] playerUpdateRequests() {
		return playerUpdateRequests;
	}

	public int[] npcUpdateRequests() {
		return npcUpdateRequests;
	}

	public int playerUpdateReqPtr() {
		return playerUpdateReqPtr;
	}

	public void playerUpdateReqPtr(int i) {
		playerUpdateReqPtr = i;
	}

	public int npcUpdateReqPtr() {
		return npcUpdateReqPtr;
	}

	public void npcUpdateReqPtr(int i) {
		npcUpdateReqPtr = i;
	}

	public int[] newlyAdded() {
		return newlyAdded;
	}

	public int newlyAddedPtr() {
		return newlyAddedPtr;
	}

	public void calculateLooks() {
		byte[] l = ((Player)entity).looks().get();
		looksBlock = new byte[l.length + 1];
		for (int i = 0; i < l.length; i++) {
			looksBlock[i + 1] = (byte) (l[i] + 128);
		}
		looksBlock[0] = (byte) l.length;
		addFlag(Flag.LOOKS.value);
	}
	
	public void shout(String text) {
    	shoutSet = new byte[text.length() + 1];
        RSBuffer buffer = new RSBuffer(Unpooled.wrappedBuffer(shoutSet));
        buffer.get().writerIndex(0);
        buffer.writeString(text);
        addFlag(Flag.SHOUT.value);
	}

	public void animation(int id, int delay) {
		RSBuffer buffer = new RSBuffer(Unpooled.wrappedBuffer(animationSet));
		buffer.get().writerIndex(0);
		buffer.writeLEShort(id);
		buffer.writeByteA(delay);

		addFlag(Flag.ANIMATION.value);
	}

	public void graphic(int id, int height, int delay) {
		RSBuffer buffer = new RSBuffer(Unpooled.wrappedBuffer(graphicSet));
		buffer.get().writerIndex(0);
		buffer.writeShort(id);
		buffer.writeLEInt(height << 16 | delay);

		addFlag(Flag.GRAPHIC.value);
	}

	@Override
	public void hit(int type, int value) {
		if (hasFlag(Flag.HIT.value)) {
			RSBuffer buffer = new RSBuffer(Unpooled.wrappedBuffer(hitSet2));
			buffer.get().writerIndex(0);
			buffer.writeLEShortA(value);
			buffer.writeByte(type);
			buffer.writeByteS(entity.hp());//current bar value
			buffer.writeByteN(entity.maxHp()); // max bar value

			addFlag(Flag.HIT2.value);
		} else {
			RSBuffer buffer = new RSBuffer(Unpooled.wrappedBuffer(hitSet));
			buffer.get().writerIndex(0);
			buffer.writeLEShortA(value);
			buffer.writeByteA(type);
			buffer.writeByte(entity.hp());//current bar value
			buffer.writeByteA(entity.maxHp()); // max bar value

			addFlag(Flag.HIT.value);
		}
	}

	@Override
	public void facetile(Tile tile) {
		RSBuffer buffer = new RSBuffer(Unpooled.wrappedBuffer(facetile));
		buffer.get().writerIndex(0);

		buffer.writeLEShort(tile.x);
		buffer.writeLEShort(tile.z);

		addFlag(Flag.FACE_TILE.value);
	}

	@Override
	public void faceEntity(Entity e) {
		RSBuffer buffer = new RSBuffer(Unpooled.wrappedBuffer(faceEntitySet));
		buffer.get().writerIndex(0);
		buffer.writeLEShortA(e == null ? -1 : e.isNpc() ? e.index() : (e.index() + 32768));

		addFlag(Flag.FACE_ENTITY.value);
	}

	public void forceMove(ForceMovement move) {
		RSBuffer buffer = new RSBuffer(Unpooled.wrappedBuffer(forcemove));
		buffer.get().writerIndex(0);

		buffer.writeByteA(move.dx1);
		buffer.writeByteN(move.dz1);
		buffer.writeByteN(move.dx2);
		buffer.writeByteS(move.dz2);
		buffer.writeShort(move.speed1);
		buffer.writeLEShortA(move.speed2);
		buffer.writeByteA(move.direction);

		addFlag(Flag.FORCE_MOVE.value);
	}

	public void publicChatMessage(ChatMessage message) {
		RSBuffer buffer = new RSBuffer(Unpooled.buffer(256));
		buffer.get().writerIndex(0);
		buffer.writeLEShortA((message.colors() << 8) | message.effects());
		buffer.writeByteA(((Player)entity).getPrivilege().ordinal());// icon?
		buffer.writeByteN(0);// icon?

		byte[] huffmandata = new byte[256];
		int len = entity.world().server().huffman().encode(message.text(), huffmandata);

		buffer.writeByteN(len + 1);
		buffer.writeCompact(message.text().length());
		buffer.get().writeBytes(huffmandata, 0, len);

		publicChatBlock = new byte[buffer.get().writerIndex()];
		System.arraycopy(buffer.get().array(), 0, publicChatBlock, 0, publicChatBlock.length);
		addFlag(Flag.CHAT.value);
	}

	public byte[] looksBlock() {
		return looksBlock;
	}

	public byte[] chatMessageBlock() {
		return publicChatBlock;
	}

	public byte[] animationSet() {
		return animationSet;
	}

	public byte[] graphicSet() {
		return graphicSet;
	}

	public void clear() {
		super.clear();
		playerUpdateReqPtr = 0;
		newlyAddedPtr = 0;
		npcUpdateReqPtr = 0;
	}

	public boolean hasInView(int index) {
		for (int i=0; i<localPlayerPtr; i++)
			if (localPlayerIndices[i] == index)
				return true;
		return false;
	}

	public boolean hasNpcInView(int index) {
		for (int i=0; i<localNpcPtr; i++)
			if (localNpcIndices[i] == index)
				return true;
		return false;
	}

	public boolean isNewlyAdded(int index) {
		for (int i=0; i<newlyAddedPtr; i++)
			if (newlyAdded[i] == index)
				return true;
		return false;
	}

	public void newlyAddedPtr(int p0) {
		newlyAddedPtr = p0;
	}

	public static enum Flag {
		LOOKS(0x8),
		ANIMATION(0x10),
		GRAPHIC(0x100),
		HIT(0x40),
		HIT2(0x200),
		FACE_ENTITY(0x1),
		FACE_TILE(0x4),
		CHAT(0x20),
		FORCE_MOVE(0x400),
		SHOUT(2);

		public int value;

		private Flag(int v) {
			value = v;
		}
	}

}
