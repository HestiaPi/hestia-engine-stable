package uk.co.jaynne.datasource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class to help make SQL queries
 * @author James Cooke
 *
 */
public class SqlStatement {
	private Statement getStatement() throws SQLException {
		Connection connection = null;
		Statement statement = null;

		try {
			connection = DriverManager.getConnection(
					DbConfig.URL, DbConfig.USER, DbConfig.PASSWORD);
			statement = connection.createStatement();
			return statement;
			
		} catch (SQLException ex) {
			throw ex; 
		} 
	}

	/**
	 * Execute a query that returns a ResultSet
	 * @param sql
	 * @return the ResultSet
	 * @throws SQLException
	 */
	public ResultSet query(String sql) throws SQLException {
		try {
			Statement statement = getStatement();
			return statement.executeQuery(sql); //return the query
			
		} catch (SQLException ex) {
			throw ex; 
		} 
	}
	
	/**
	 * Execute an update such as INSERT, DELTE OR UPDATE
	 * @param sql
	 * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing
	 * @throws SQLException
	 */
	public int update(String sql) throws SQLException {
		try {
			Statement statement = getStatement();
			int result = statement.executeUpdate(sql);
			statement.close();
			return result;
			
		} catch (SQLException ex) {
			throw ex; 
		} 
	}
}
