package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import io.netty.buffer.Unpooled;

public class MakeMinimapOrWorldOrbBlack106 implements Command {

	private int value;
	
	public MakeMinimapOrWorldOrbBlack106(int value) {
		this.value = value;
	}

	@Override 
	public RSBuffer encode(Player player) {
		//RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(4));
		RSBuffer buffer = new RSBuffer(Unpooled.buffer());

		buffer.packet(106);
		buffer.writeByte(value);

		return buffer;
	}
}
