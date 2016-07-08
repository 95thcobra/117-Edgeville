package edgeville.net.message.game.decoders;

import edgeville.aquickaccess.actions.ObjectClick2Action;
import edgeville.aquickaccess.events.ClickObjectEvent;
import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.io.RSBuffer;
import edgeville.model.AttributeKey;
import edgeville.model.Tile;
import edgeville.model.entity.PathQueue;
import edgeville.model.entity.Player;
import edgeville.model.map.MapObj;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon on 8/23/2015.
 */
@PacketInfo(size = 7)
public class ObjectAction2 implements Action {

    private int id;
    private int x;
    private int z;
    private boolean run;

    @Override
    public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
        id = buf.readUShort();
        run = buf.readByte() == 1;
        z = buf.readUShortA();
        x = buf.readUShortA();
    }

    @Override
    public void process(Player player) {
        MapObj obj = player.world().objById(id, x, z, player.getTile().level);

        if (obj == null)
            return;

        if (player.isDebug())
            player.message("Interacting with object %d at [%d, %d]", id, x, z);

        if (!player.locked() && !player.dead()) {
            //player.stopActions(true);
            //player.putattrib(AttributeKey.INTERACTION_OBJECT, obj);
            //player.putattrib(AttributeKey.INTERACTION_OPTION, 2);
            //player.world().server().scriptExecutor().executeScript(player, ObjectInteraction.script);


            player.stopActions(true);
            player.putAttribute(AttributeKey.INTERACTION_OBJECT, obj);
            player.putAttribute(AttributeKey.INTERACTION_OPTION, 1);

            player.walkTo(obj, PathQueue.StepType.REGULAR);
            player.faceObj(obj);
            Tile targetTile = player.pathQueue().getTargetTile();
            player.world().getEventHandler().addEvent(player, new Event() {
                @Override
                public void execute(EventContainer container) {
                    if (player.getTile().distance(targetTile) <= 3) {
                        new ObjectClick2Action().handleObjectClick(player, obj);
                        container.stop();
                    }
                }
            });
        }
    }

}
