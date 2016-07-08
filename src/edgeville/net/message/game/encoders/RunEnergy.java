package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.GroundItem;
import edgeville.model.entity.Player;

/**
 * @author Simon on 8/22/2015.
 */
public class RunEnergy implements Command {

	private int value;
	
	public RunEnergy(int value) {
		this.value = value;;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer packet = new RSBuffer(player.channel().alloc().buffer(1)).packet(161);

		packet.writeByte(50);

		return packet;
	}

}

