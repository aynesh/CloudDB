package app_kvServer;

import app_kvServer.impl.FIFOCache;
import app_kvServer.impl.LFUCache;
import app_kvServer.impl.LRUCache;

public class CacheManager {

	public static Cache instantiateCache(String type, int size) {
		switch(type) {
		case "FIFO": return new FIFOCache(size);
		case "LFU": return new LFUCache(size);
		case "LRU": return new LRUCache(size);
		default: return new LRUCache(size);
		}
	}
}
