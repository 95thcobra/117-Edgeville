package edgeville.net.message.game.decoders;

import edgeville.fs.ItemDefinition;
import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import edgeville.util.L10n;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon on 8/9/2015.
 */
@PacketInfo(size = 2)
public class ExamineItem implements Action {

	private int id;

	@Override public void process(Player player) {
		player.message(player.world().examineRepository().item(id));
	}

	@Override public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		id = buf.readUShortA();
	}

}
