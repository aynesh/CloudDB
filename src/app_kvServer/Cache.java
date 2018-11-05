package app_kvServer;


public interface Cache {
	/**
	 * @param key
	 * @return value for the key or null if not found.
	 */
	public String get(String key);
	/**
	 * @param key and value
	 * @return
	 */
	public void add(String key, String value);
	/**
	 * @param key
	 * @return true of false
	 */
	public boolean contains(String key);
	/**
	 * @param key
	 * @return
	 */
	public void delete(String key);	
}