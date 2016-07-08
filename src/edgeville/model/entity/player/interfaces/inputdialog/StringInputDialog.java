package edgeville.model.entity.player.interfaces.inputdialog;

import edgeville.model.entity.Player;
import edgeville.model.entity.player.interfaces.InputDialog;
import edgeville.net.message.game.encoders.InvokeScript;

public abstract class StringInputDialog extends InputDialog {

private Player player;

	public StringInputDialog(Player player) {
		super(player);
		this.player = player;
	}
	
	public void send(String title) {
		player.write(new InvokeScript(110, new Object[] { title }));
		player.setInputDialog(this);
	}

	public abstract void doAction(String value);

}
