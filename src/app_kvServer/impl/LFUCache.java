package app_kvServer.impl;

import java.util.ArrayList;
import java.util.HashMap;


import app_kvServer.Cache;

public class LFUCache implements Cache {
	
	//public class Pair {public String key; public Integer n;};
	private HashMap<String,String> cacheItems;
	private ArrayList<String> ordering;
	private ArrayList<Integer> frequency;
	int totalSize = 0;
	
	public LFUCache(int size) {
		cacheItems = new HashMap<String,String>();
		ordering = new ArrayList<String>();
		frequency = new ArrayList<Integer>();
		totalSize = size;
	}

	@Override
	public synchronized boolean contains(String key) {
		if(cacheItems.containsKey(key)) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public synchronized String get(String key) {
		if(ordering.contains(key)) {
			reorder(key);
		}
		return cacheItems.get(key);
	}

	@Override
	public synchronized void add(String key, String value) {
		cacheItems.put(key, value);
		if(!ordering.contains(key)) {
			if(totalSize == ordering.size()) {
				cacheItems.remove(ordering.get(0));
				ordering.remove(0);
				frequency.remove(0);
			}
			ordering.add(0,key);
			frequency.add(0,1);
		}
		else {
			reorder(key);
		}
	}
	
	@Override
	public synchronized void delete(String key) {
		cacheItems.remove(key);
		if(ordering.contains(key)) {
			int index = ordering.indexOf(key);
			ordering.remove(index);
			frequency.remove(index);
		}
	}
	
	public synchronized void reorder(String key){
		int index = ordering.indexOf(key);
		int curr = frequency.get(index);
		curr = curr+1;
		
		int newIndex = index + 1;
		while(newIndex<frequency.size() && frequency.get(newIndex)<curr) {
			newIndex++;
		}
		newIndex--;
		
		ordering.remove(index);
		ordering.add(newIndex, key);
		frequency.remove(index);
		frequency.add(newIndex,curr);
	}
}
