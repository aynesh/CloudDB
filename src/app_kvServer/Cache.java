package app_kvServer;


public interface Cache {
	public String get(String key);
	public void add(String key, String value);
	public boolean contains(String key);
	void delete(String key);	
}