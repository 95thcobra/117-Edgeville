package edgeville.model.entity.player.interfaces.inputdialog;

import edgeville.model.entity.Player;
import edgeville.model.entity.player.interfaces.InputDialog;
import edgeville.net.message.game.encoders.InvokeScript;

public abstract class NumberInputDialog extends InputDialog {

	private Player player;
	
	public NumberInputDialog(Player player) {
		super(player);
		this.player = player;
	}

	public void send() {
		player.write(new InvokeScript(108, new Object[] { "Enter Amount:" }));
		player.setInputDialog(this);
	}

	public abstract void doAction(int value);
}
