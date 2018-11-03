package app_kvServer.impl;

import java.util.HashMap;
import java.util.LinkedList;

import app_kvServer.Cache;

public class FIFOCache implements Cache {
	
	private LinkedList<HashMap<String,String>> cacheItems;
	int totalSize = 0;
	
	public FIFOCache(int size) {
		cacheItems = new LinkedList<HashMap<String,String>>();
		totalSize = size;
	}

	@Override
	public String get(String key) {
		for(HashMap<String,String> item: cacheItems) {
			if( item.containsKey(key) ) {
				return item.get(key);
			}
		}
		return null;
	}

	@Override
	public void add(String key, String value) {
		HashMap<String,String> item = new HashMap<String,String>();
		item.put(key, value);
		if(cacheItems.size() == totalSize) {
			cacheItems.removeLast();
			cacheItems.addFirst(item);
		} else {
			cacheItems.addFirst(item);
		}
		
	}
}
