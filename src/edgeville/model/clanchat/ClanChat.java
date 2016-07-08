package edgeville.model.clanchat;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edgeville.model.entity.Player;
import edgeville.model.entity.player.Privilege;
import edgeville.net.message.game.encoders.AddClanChatMessage;

public class ClanChat {
	private Player owner;
	private String name;
	private List<Player> players;

	public ClanChat(Player owner, String name, List<Player> players) {
		this.owner = owner;
		this.players = players;
		this.name = name;
		players.add(owner);
	}

	public void addPlayer(Player player) {
		if (players.contains(player)) {
			player.messageFilterable("You're already in %s.", name);
			return;
		}
		players.add(player);
		player.setClanChat(this);
		player.messageFilterable("You have joined %s.", name);
	}

	public void message(Player sender, String message) {
		int icon;
		if (sender.getPrivilege() == Privilege.MODERATOR) {
			icon = 1;
		} else if (sender.getPrivilege() == Privilege.ADMIN) {
			icon = 2;
		} else {
			icon = 0;
		}
		players.forEach(p -> p.write(new AddClanChatMessage(StringUtils.capitalize(sender.getUsername()), "Help", icon, 301, message)));
	}

	public void save() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("owner", owner.getUsername());

		JsonArray jsonArray = new JsonArray();
		for (Player player : players) {
			jsonArray.add(player.getUsername());
		}

		File characterFile = new File("data/clanchats", owner.getUsername() + ".json");
		try (FileWriter out = new FileWriter(characterFile)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			out.write(gson.toJson(jsonObject));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		String members = "";
		for (Player player : players) {
			members += player.getUsername();
		}
		return members;
	}

	public void info(String string) {
		players.forEach(p -> p.write(new AddClanChatMessage("INFO", "Help", 3, 301, "Info")));
		
	}
}
