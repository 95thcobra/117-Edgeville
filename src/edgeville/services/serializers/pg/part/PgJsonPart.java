package edgeville.services.serializers.pg.part;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edgeville.model.entity.Player;

/**
 * @author Simon on 8/10/2015.
 */
public interface PgJsonPart {

	public void decode(Player player, ResultSet resultSet) throws SQLException;

	public void encode(Player player, PreparedStatement characterUpdateStatement) throws SQLException;

}
