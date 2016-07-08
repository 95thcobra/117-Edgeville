package edgeville.services.serializers.pg.part;

import com.google.gson.*;

import edgeville.model.entity.Player;
import edgeville.model.item.Item;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Simon on 8/25/2015.
 */
public class VarpsPart implements PgJsonPart {

	private JsonParser parser = new JsonParser();
	private Gson gson = new Gson();

	@Override
	public void decode(Player player, ResultSet resultSet) throws SQLException {
		JsonArray varparray = parser.parse(resultSet.getString("varps")).getAsJsonArray();
		for (JsonElement varp : varparray) {
			JsonObject item = varp.getAsJsonObject();
			player.getVarps().getVarps()[item.get("id").getAsInt()] = item.get("val").getAsInt();
		}
	}

	@Override
	public void encode(Player player, PreparedStatement characterUpdateStatement) throws SQLException {
		player.getVarps().presave();

		JsonArray varparray = new JsonArray();

		int[] v = player.getVarps().getVarps();
		for (int i = 0; i < 2000; i++) {
			if (v[i] != 0) {
				JsonObject obj = new JsonObject();
				obj.add("id", new JsonPrimitive(i));
				obj.add("val", new JsonPrimitive(v[i]));
				varparray.add(obj);
			}
		}

		characterUpdateStatement.setString(9, gson.toJson(varparray));
	}

}
