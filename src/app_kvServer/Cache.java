package app_kvServer;


public interface Cache {
	/**
	 * @param key the key.
	 * @return value for the key or null if not found.
	 */
	public String get(String key);
	/**
	 * @param key and value
	 * @param value - value
	 */
	public void add(String key, String value);
	/**
	 * @param key key for the item to retrive 
	 * @return true of false
	 */
	public boolean contains(String key);
	/**
	 * @param key key to delete.
	 */
	public void delete(String key);	
}