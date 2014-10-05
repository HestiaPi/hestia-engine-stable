package uk.co.jaynne.dataobjects;

/**
 * Data storage class describing a config item
 * @author James Cooke
 */
public class ConfigObject {
	private String key = null;
	private String type = null;
	private String stringValue = null;
	private boolean boolValue = false;
	private int intValue = 0;
	private long longValue = 0;
	private float floatValue = 0.00f;
	
	/**
	 * Create a config item with a string type
	 * @param key
	 * @param type
	 * @param stringValue
	 */
	public ConfigObject(String key, String stringValue) {
		this.key = key;
		this.type = "String";
		this.stringValue = stringValue;
	}
	
	public ConfigObject(String key, boolean boolValue) {
		this.key = key;
		this.type = "boolean";
		this.boolValue = boolValue;
	}
	
	public ConfigObject(String key, int intValue) {
		this.key = key;
		this.type = "int";
		this.intValue = intValue;
	}
	
	public ConfigObject(String key, long longValue) {
		this.key = key;
		this.type = "int";
		this.longValue = longValue;
	}

	public ConfigObject(String key, float floatValue) {
		this.key = key;
		this.type = "float";
		this.floatValue = floatValue;
	}
	
	public String getKey() {
		return key;
	}

	public String getType() {
		return type;
	}

	public String getStringValue() {
		return stringValue;
	}

	public boolean getBoolValue() {
		return boolValue;
	}

	public int getIntValue() {
		return intValue;
	}
	
	public long getLongValue() {
		return longValue;
	}
	
	public float getFloatValue() {
		return floatValue;
	}
}
