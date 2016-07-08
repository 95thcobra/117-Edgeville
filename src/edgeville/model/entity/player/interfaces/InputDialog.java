package edgeville.model.entity.player.interfaces;

import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.InvokeScript;

public abstract class InputDialog {

	private final Player player;

	public InputDialog(Player player) {
		this.player = player;
	}
}
