package uk.co.jaynne.datasource.interfaces;

import uk.co.jaynne.dataobjects.ConfigObject;
/**
 * Interface to retrieve and update config items from a SQL database
 * @author James Cooke
 */
public interface ConfigSource {
	
	public ConfigObject get(String key);
	
	public int set(String key, String value);
	
	public int set(String key, int value);
	
	public int set(String key, boolean value);
	
	public int set(String key, long value);
	
	public int set(String key, float value);
}
