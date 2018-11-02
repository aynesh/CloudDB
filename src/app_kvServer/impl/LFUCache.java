package app_kvServer.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import app_kvServer.Cache;

public class LFUCache implements Cache {
	
	   class CacheEntry
	    {
	        private HashMap<String,String> data;
	        private int frequency;

	        // default constructor
	        private CacheEntry()
	        {}

	        public HashMap<String,String> getData() {
	            return data;
	        }
	        public void setData(HashMap<String,String> data) {
	            this.data = data;
	        }

	        public int getFrequency() {
	            return frequency;
	        }
	        public void setFrequency(int frequency) {
	            this.frequency = frequency;
	        }       

	    }

	    private static int initialCapacity = 10;

	    private static LinkedHashMap<String, CacheEntry> cacheMap = new LinkedHashMap<String, CacheEntry>();

	    public LFUCache(int initialCapacity)
	    {
	        this.initialCapacity = initialCapacity;
	    }

	    @Override
	    public void add(String key, String value)
	    {
	    	HashMap<String,String> data = new HashMap<String,String>();
	    	data.put(key, value);
	        if(!isFull())
	        {
	            CacheEntry temp = new CacheEntry();
	            temp.setData(data);
	            temp.setFrequency(0);
	            cacheMap.put(key, temp);
	        }
	        else
	        {
	            String entryKeyToBeRemoved = getLFUKey();
	            cacheMap.remove(entryKeyToBeRemoved);

	            CacheEntry temp = new CacheEntry();
	            temp.setData(data);
	            temp.setFrequency(0);

	            cacheMap.put(key, temp);
	        }
	    }

	    public String getLFUKey()
	    {
	        String key = "";
	        int minFreq = Integer.MAX_VALUE;

	        for(Map.Entry<String, CacheEntry> entry : cacheMap.entrySet())
	        {
	            if(minFreq > entry.getValue().frequency)
	            {
	                key = entry.getKey();
	                minFreq = entry.getValue().frequency;
	            }           
	        }

	        return key;
	    }

	    @Override
	    public String get(String key)
	    {
	        if(cacheMap.containsKey(key))  // cache hit
	        {
	            CacheEntry temp = cacheMap.get(key);
	            temp.frequency++;
	            cacheMap.put(key, temp);
	            return temp.data.get(key);
	        }
	        return null; // cache miss
	    }

	    public static boolean isFull()
	    {
	        if(cacheMap.size() == initialCapacity)
	            return true;

	        return false;
	    }
}
