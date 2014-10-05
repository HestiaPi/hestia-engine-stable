package uk.co.jaynne.datasource;
import java.sql.ResultSet;
import java.sql.SQLException;

import uk.co.jaynne.dataobjects.ConfigObject;
import uk.co.jaynne.datasource.interfaces.ConfigSource;

/**
 * Class to retrieve and update config items from a SQL database
 * @author James Cooke
 */
public class ConfigSqlSource implements ConfigSource{
	
	private static String TABLE = "configuration"; 
	private static String KEY = "key"; 
	private static String VALUE = "value"; 
	private static String TYPE = "type"; 
	
	/**
	 * Get a config object based on its key.
	 * Returns a ConfigObject or null if there is no object of that key
	 */
	public ConfigObject get(String key) {
		String sql = "SELECT * FROM `" + TABLE + "` WHERE `" + KEY + "` = '" + key + "'";
		SqlStatement smt = new SqlStatement();
		
		ConfigObject object = null;
		try {
			ResultSet resultSet = smt.query(sql);
		
			while (resultSet.next()) {
				String value = resultSet.getString(VALUE);
				String type = resultSet.getString(TYPE);
				switch (type) {
					case "int": 
						object = new ConfigObject(key, Integer.parseInt(value));
						break;
					case "long": 
						object = new ConfigObject(key, Long.parseLong(value));
						break;
					case "boolean": 
						object = new ConfigObject(key, Boolean.parseBoolean(value));
						break;
					case "float": 
						object = new ConfigObject(key, Float.parseFloat(value));
						break;
					default: 
						object = new ConfigObject(key, value); //must be string
						break;
				}
			}
			
			resultSet.close(); //tidy up
		} catch (SQLException e) {
			return null;
		}
		return object;
	}
	
	private int set(String key, String value, String type) {
		String sql = 
				"UPDATE `" + TABLE + "` " +
				"SET `" + VALUE + "` = '" + value + "', `" + TYPE +"` = '" + type + "' " + 
				"WHERE `" + KEY + "` = '" + key + "'";
		SqlStatement smt = new SqlStatement();
		
		try {
			return smt.update(sql);
		} catch (SQLException e) {
			return 0;
		}
	}
	
	/**
	 * Update a string item
	 * @param key the items key
	 */
	public int set(String key, String value) {
		return set(key, value, "String");
	}

	/**
	 * Update an int item
	 * @param key the items key
	 */
	public int set(String key, int value) {
		return set(key, Integer.toString(value), "int");
	}
	
	/**
	 * Update a boolean item
	 * @param key the items key
	 */
	public int set(String key, boolean value) {
		return set(key, Boolean.toString(value), "boolean");
	}
	
	/**
	 * Update a long item
	 * @param key the items key
	 */
	public int set(String key, long value) {
		return set(key, Long.toString(value), "long");
	}
	
	/**
	 * Update a float item
	 * @param key the items key
	 */
	public int set(String key, float value) {
		return set(key, Float.toString(value), "float");
	}	
}
