package app_kvServer.impl;

import java.util.ArrayList;
import java.util.HashMap;


import app_kvServer.Cache;

public class FIFOCache implements Cache {
	
	private HashMap<String,String> cacheItems;
	private ArrayList<String> ordering;
	int totalSize = 0;
	
	public FIFOCache(int size) {
		cacheItems = new HashMap<String,String>();
		ordering = new ArrayList<String>();
		totalSize = size;
	}

	@Override
	public boolean contains(String key) {
		if(cacheItems.containsKey(key)) {
			return true;
		}
		return false;
	}
	
	@Override
	public String get(String key) {
		return cacheItems.get(key);
	}

	@Override
	public void add(String key, String value) {
		cacheItems.put(key, value);
		if(!ordering.contains(key)) {
			ordering.add(key);
		}
		if(totalSize < ordering.size()) {
			cacheItems.remove(ordering.get(0));
			ordering.remove(0);
		}
		
	}
	
	@Override
	public void delete(String key) {
		cacheItems.remove(key);
		if(ordering.contains(key)) {
			ordering.remove(key);
		}
	}
}
