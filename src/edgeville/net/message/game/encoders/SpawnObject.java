package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.model.map.MapObj;

/**
 * @author Simon on 8/29/2015.
 */
public class SpawnObject implements Command {

	private MapObj obj;

	public SpawnObject(MapObj obj) {
		this.obj = obj;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(5)).packet(63);

		buffer.writeByteA((obj.type() << 2) | obj.rot());
		buffer.writeByteS(((obj.tile().x & 7) << 4) | (obj.tile().z & 7));
		buffer.writeShortA(obj.id());

		return buffer;
	}


}
