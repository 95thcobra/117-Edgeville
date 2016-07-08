package edgeville.services.serializers.pg.part;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edgeville.model.Tile;
import edgeville.model.entity.Player;

/**
 * @author Simon on 8/10/2015.
 */
public class TilePart implements PgJsonPart {

	@Override
	public void decode(Player player, ResultSet resultSet) throws SQLException {
		player.setTile(new Tile(resultSet.getInt("x"), resultSet.getInt("z"), resultSet.getInt("level")));
	}

	@Override
	public void encode(Player player, PreparedStatement characterUpdateStatement) throws SQLException {
		characterUpdateStatement.setInt(1, player.getTile().x);
		characterUpdateStatement.setInt(2, player.getTile().z);
		characterUpdateStatement.setInt(3, player.getTile().level);
	}

}
