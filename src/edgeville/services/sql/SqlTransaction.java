package edgeville.services.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Simon on 9/18/2015.
 */
public abstract class SqlTransaction {

	private int attempts;

	public abstract void execute(Connection connection) throws Exception;

	public int attempts() {
		return attempts;
	}

	public void increaseAttempts() {
		attempts++;
	}

}
