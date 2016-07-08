package edgeville.model.clanchat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edgeville.model.Tile;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Privilege;
import edgeville.services.serializers.PlayerLoadResult;

public class ClanChatHandler {

	public ClanChat getClanChat(Player player) {
		try {
			File clanchatFolder = new File("data/clanchats", player.getUsername() + ".json");
			JsonElement element = new JsonParser().parse(new InputStreamReader(new FileInputStream(clanchatFolder)));
			JsonObject jsonObjectClan = element.getAsJsonObject();

			JsonArray jsonPlayers = jsonObjectClan.get("players").getAsJsonArray();

			List<Player> players = new ArrayList<Player>();
			for (JsonElement ele : jsonPlayers) {
				Player p = player.world().getPlayerByName(ele.getAsString()).get();
				players.add(p);
			}

			ClanChat clanChat = new ClanChat(player, player.getUsername() +"'s clanchat", players);

			return clanChat;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
