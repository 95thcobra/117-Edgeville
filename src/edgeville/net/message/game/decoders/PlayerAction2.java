package edgeville.net.message.game.decoders;

import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.io.RSBuffer;
import edgeville.model.AttributeKey;
import edgeville.model.Tile;
import edgeville.model.entity.PathQueue;
import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import edgeville.script.TimerKey;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon on 8/12/2015.
 */
@PacketInfo(size = 3)
public class PlayerAction2 implements Action {

    private boolean run;
    private int index;

    @Override
    public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
        index = buf.readULEShort();
        run = buf.readByteS() == 1;
    }

    @Override
    public void process(Player player) {
        player.stopActions(true);

        Player other = player.world().players().get(index);
        if (other == null) {
            player.message("Unable to find player.");
        } else {
            if (!player.locked() && !player.dead() && !other.dead()) {
                player.face(other);

                //player.putattrib(AttributeKey.TARGET, index);
                //player.world().server().scriptExecutor().executeScript(player, PlayerFollowing.script);
                playerFollowing(player, other);
            }
        }
    }

    private void playerFollowing(Player player, Player other) {
        player.world().getEventHandler().addEvent(player, new Event() {
            @Override
            public void execute(EventContainer container) {
  

                if (player.frozen() || player.stunned()) {
                    return;
                }

                /*Tile lastTile = other.pathQueue().lastStep();
                if (lastTile == null) {
                    //container.stop();
                    return;
                }
                player.walkTo(lastTile.x, lastTile.z, PathQueue.StepType.REGULAR);*/
                
                
                player.pathQueue().clear(); // This to prevent weird hickups
				player.stepTowards(other);
            }
        });
    }

}
