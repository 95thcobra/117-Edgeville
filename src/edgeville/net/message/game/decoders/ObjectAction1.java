package edgeville.net.message.game.decoders;

import edgeville.aquickaccess.events.ClickObjectEvent;
import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.io.RSBuffer;
import edgeville.model.AttributeKey;
import edgeville.model.ForceMovement;
import edgeville.model.Tile;
import edgeville.model.entity.PathQueue;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Skills;
import edgeville.model.map.MapObj;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon on 8/23/2015.
 * Modified by Simon on 4/3/2016.
 */
@PacketInfo(size = 7)
public class ObjectAction1 implements Action {

    private int id;
    private int x;
    private int z;
    private boolean run;

    @Override
    public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
        id = buf.readUShortA();
        run = buf.readByteS() == 1;
        z = buf.readUShort();
        x = buf.readUShortA();
    }

    @Override
    public void process(Player player) {
        MapObj obj = player.world().objById(id, x, z, player.getTile().level);

        if (obj == null)
            return;

        if (player.isDebug()) {
            player.message("Interacting with object %d at [%d, %d]", id, x, z);
        }

        if (!player.locked() && !player.dead()) {
            player.stopActions(true);
            player.putAttribute(AttributeKey.INTERACTION_OBJECT, obj);
            player.putAttribute(AttributeKey.INTERACTION_OPTION, 1);

            player.walkTo(obj, PathQueue.StepType.REGULAR);
            player.faceObj(obj);


            Tile targetTile = player.pathQueue().getTargetTile();
            player.world().getEventHandler().addEvent(player, new ClickObjectEvent(player, obj, targetTile));



            /*player.world().getEventHandler().addEvent(player, 1, new Event() {
                @Override
                public void execute(EventContainer container) {
                    if (player.tile().equals(lastTile)) {
                        container.stop();
                    }
                }

                @Override
                public void stop() {
                    handleObject(player, obj);
                }
            });*/
        }
    }




    /**
     * TODO: Fix concurrent modification exception.
     * @param player
     */
   /* private void handleDraynorWallClimb(Player player) {
        // Is the player on the correct side of the wall?
        if (player.tile().z <= 3255)
            return;

        // Start event
        player.event(new Event() {
            int count = 0;

            @Override
            public void execute(EventContainer container) {
                switch (count) {
                    case 0:
                        player.lock();
                        player.pathQueue().step(3088, 3256);
                        player.animate(2583, 20);
                        player.forceMove(new ForceMovement(0, 1, 25, 30));
                        break;
                    case 1:
                        player.animate(2585);
                        break;
                    case 2:
                        player.pathQueue().step(3088, 3255);
                        player.forceMove(new ForceMovement(0, 1, 17, 26));
                        break;
                    case 3:
                        player.skills().addXp(Skills.AGILITY, 10.0);
                        player.unlock();
                        container.stop();
                        break;
                }
                count++;
            }
        });
    }*/

}
