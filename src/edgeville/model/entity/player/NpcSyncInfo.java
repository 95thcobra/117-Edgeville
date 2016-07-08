package edgeville.model.entity.player;

import edgeville.io.RSBuffer;
import edgeville.model.ChatMessage;
import edgeville.model.Entity;
import edgeville.model.Tile;
import edgeville.model.entity.Npc;
import edgeville.model.entity.Player;
import edgeville.model.entity.SyncInfo;
import io.netty.buffer.Unpooled;

/**
 * @author Simon on 8/23/2014.
 */
public class NpcSyncInfo extends SyncInfo {

    public NpcSyncInfo(Npc npc) {
        super(npc);
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
        buffer.writeShort(id);
        buffer.writeByteN(delay);
        
        addFlag(Flag.ANIMATION.value);
    }

    public void graphic(int id, int height, int delay) {
        RSBuffer buffer = new RSBuffer(Unpooled.wrappedBuffer(graphicSet));
        buffer.get().writerIndex(0);
        buffer.writeLEShort(id);
        buffer.writeInt(height << 16 | delay);

        addFlag(Flag.GRAPHIC.value);
    }

    @Override
    public void hit(int type, int value) {
        if (hasFlag(NpcSyncInfo.Flag.HIT.value)) {
            RSBuffer buffer = new RSBuffer(Unpooled.wrappedBuffer(hitSet2NPC));
            buffer.get().writerIndex(0);
            buffer.writeByteS(value);
            buffer.writeByteN(type);
            buffer.writeLEShort(entity.hp());
            buffer.writeLEShort(entity.maxHp());
            addFlag(NpcSyncInfo.Flag.HIT2.value);
        } else {
            RSBuffer buffer = new RSBuffer(Unpooled.wrappedBuffer(hitSetNPC));
            buffer.get().writerIndex(0);
            buffer.writeByteN(value);
            buffer.writeByteN(type);
            buffer.writeShortA(entity.hp());
            buffer.writeLEShort(entity.maxHp());
            addFlag(NpcSyncInfo.Flag.HIT.value);
        }
    }

    @Override
    public void facetile(Tile tile) {

    }

    @Override
    public void faceEntity(Entity e) {
        //TODO
    }

    public byte[] animationSet() {
        return animationSet;
    }

    public byte[] graphicSet() {
        return graphicSet;
    }
    
    public byte[] shoutSet() {
        return shoutSet;
    }

    public static enum Flag {
        ANIMATION(0x10),
        GRAPHIC(0x40),
        HIT(0x1),
        HIT2(0x2),
        SHOUT(128);

        public int value;

        private Flag(int v) {
            value = v;
        }
    }

}
