package edgeville.net.message.game.decoders;

import io.netty.channel.ChannelHandlerContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import edgeville.util.GameCommands;

/**
 * @author Simon on 8/23/2014.
 */
@PacketInfo(size = -1)
public class ConsoleAction implements Action {

	private static final Logger logger = LogManager.getLogger(ConsoleAction.class);

	private String command;

	@Override public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		command = buf.readString();
	}

	@Override public void process(Player player) {
		try {
			if (!player.dead())
				GameCommands.process(player, command);
		} catch (Exception e) {
			player.message("Error processing command %s: %s (%s).", command, e.getClass(), e.getMessage());
		}
	}
}
