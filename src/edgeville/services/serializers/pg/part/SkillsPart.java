package edgeville.services.serializers.pg.part;

import com.google.gson.*;

import edgeville.model.entity.Player;
import edgeville.model.entity.player.Skills;
import edgeville.model.item.Item;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Simon on 8/10/2015.
 */
public class SkillsPart implements PgJsonPart {

	private JsonParser parser = new JsonParser();
	private Gson gson = new Gson();

	@Override
	public void decode(Player player, ResultSet resultSet) throws SQLException {
		JsonObject inventory = parser.parse(resultSet.getString("skills")).getAsJsonObject();
		JsonArray levels = inventory.getAsJsonArray("level");
		JsonArray xps = inventory.getAsJsonArray("xp");
		for (int i=0; i< Skills.SKILL_COUNT; i++) {
			int lvl = levels.get(i).getAsInt();
			double xp = xps.get(i).getAsDouble();
			player.skills().xp()[i] = xp;
			player.skills().levels()[i] = lvl;
		}
		player.skills().recalculateCombat();
	}

	@Override
	public void encode(Player player, PreparedStatement characterUpdateStatement) throws SQLException {
		JsonArray levels = new JsonArray();
		JsonArray xps = new JsonArray();

		for (int i = 0; i < Skills.SKILL_COUNT; i++) {
			levels.add(new JsonPrimitive(player.skills().level(i)));
			xps.add(new JsonPrimitive(player.skills().xp()[i]));
		}

		JsonObject itemobj = new JsonObject();
		itemobj.add("level", levels);
		itemobj.add("xp", xps);

		characterUpdateStatement.setString(6, gson.toJson(itemobj));
	}

}
