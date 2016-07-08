package edgeville.net.message.game.decoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon
 */
@PacketInfo(size = -1)
public class JoinClanChatDialog implements Action {

	// private int size;
	private String name;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		name = buf.readString();
	}

	@Override
	public void process(Player player) {
		player.messageDebug("name %s", name);
		
		Player ccOwner = player.world().getPlayerByName(name).get();
		ccOwner.getClanChat().addPlayer(player);
		
		//ccOwner.getClanChat().info("huh");
		
		//ccOwner.message("Clanchat members: %s", ccOwner.getClanChat());
		//ccOwner.getClanChat().message(player, "Testyo");
	}
}
