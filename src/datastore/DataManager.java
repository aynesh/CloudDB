package datastore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;

import app_kvServer.Cache;
import app_kvServer.KVServer;
import common.messages.KVMessage.StatusType;

public class DataManager {
	
	public static Cache cache;
	
	
    /**
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    public static StatusType put(String key, String value) throws Exception {
        String fileName = KVServer.storagePath+key+".txt";
        StatusType type = StatusType.PUT_SUCCESS;
        FileWriter fileWriter;
        File file = new File(fileName);
        if (file.exists()){
        	type = StatusType.PUT_UPDATE;
        }
			fileWriter = new FileWriter(fileName);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
	        bufferedWriter.write(value);
	        bufferedWriter.close();
		    cache.add(key, value);
        return type;
    }
    
    
    public static File[] getAllTextFiles() {
    	File dir = new File(KVServer.storagePath);
        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        });
    }

    /**
     * @param key
     * @return
     * @throws Exception
     */
    public static String get(String key) throws Exception {
    	if(cache.contains(key)) {
    		return cache.get(key);
    	}
    	
    	String fileName = KVServer.storagePath+key+".txt";
    	FileReader fileReader = new FileReader(fileName);
    	BufferedReader bufferedReader = new BufferedReader(fileReader);
    	String value = bufferedReader.readLine();
    	bufferedReader.close();
    	cache.add(key, value);
    	return value;
    }
    
    public static void delete(String key) throws Exception {
    	String fileName = KVServer.storagePath+key+".txt";
    	File file = new File(fileName);
    	if(!file.delete()) 
        { 
    		System.out.println("Delete Exception ");
            throw new Exception();
        } 
    	if(cache.contains(key)) {
    		cache.delete(key);
    	}
    }

}
