package edgeville.services.sql;

import java.sql.Connection;

import edgeville.services.Service;

/**
 * @author Simon on 8/1/2015.
 *
 * <p>Base class for a SQL service, whether it be PgSql, MySql, Sqlite or whatever provider you're in love with.</p>
 *
 * <p>The actual service makes use of the overridden methods using whatever is compatible with the database and is not
 * forced to use ANSI SQL features only. For example, using a PgSql service may use the array support but a Sqlite
 * service may not at all.</p>
 */
public interface SqlService extends Service {

	/**
	 * Requests a database connection of this implementation's type. Whatever the underlying method does shouldn't
	 * matter; just be wary that it can either be a single instance database connection object or one that
	 * belongs to a pool.
	 *
	 * @return A connection that can be used to make queries on this database service.
	 */
	public Connection connection();

}
