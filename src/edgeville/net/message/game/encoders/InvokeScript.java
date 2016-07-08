package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;

/**
 * @author Simon on 8/22/2014.
 */
public class InvokeScript implements Command {

	private int id;
	private Object[] args;
	private String types;
	private int size;

	private String lol;

	public InvokeScript(int id, Object... args) {
		//lol = "invokescript sent: id:" + id;
		for (int i = 0; i < args.length; i++) {
			lol += " arg:" + args[i].toString() + ", ";
		}

		this.id = id;
		this.args = args;

		/* Calculate types */
		size = 1 + 2 + 4;
		char[] chars = new char[args.length];
		for (int i = 0; i < args.length; i++) {
			chars[i] = args[i] instanceof String ? 's' : 'i';
			types += args[i] instanceof String ? args[i].toString().length() + 1 : 4;
		}
		types = new String(chars);
		//System.out.println("TYPES:"+types);
		size += types.length() + 1;
	}

	@Override
	public RSBuffer encode(Player player) {
		//player.message("scriptinvoke id:" + id + " args:"+lol);

		RSBuffer buf = new RSBuffer(player.channel().alloc().buffer(size));

		buf.packet(154).writeSize(RSBuffer.SizeType.SHORT);

		////////////////
		buf.writeString(types);
		//player.message("types:" + types);
		for (int i = args.length - 1; i >= 0; i--) {
			if (args[i] instanceof String) {
				buf.writeString(((String) args[i]));
				//player.message("string:" + args[i]);
			} else {
				buf.writeInt((int) args[i]);
				//player.message("number:" + args[i]);
			}
		}
		buf.writeInt(id);
		return buf;
	}
}