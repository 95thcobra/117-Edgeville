package edgeville.services.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Simon on 9/18/2015.
 *
 * Provides a thin layer on top of the prepared statements that allows it to be reused on different connections.
 * This class is threadsafe and fully immutable.
 */
public final class SharableStatement {

	private final String query;

	public SharableStatement(String query) {
		this.query = query;
	}

	public PreparedStatement using(Connection c) throws SQLException {
		return c.prepareStatement(query);
	}

	@Override
	public String toString() {
		return "\"" + query + "\"";
	}
}
