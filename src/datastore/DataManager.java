package datastore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.log4j.Logger;

import app_kvServer.Cache;
import app_kvServer.KVServer;
import common.messages.KVMessage.StatusType;

public class DataManager {
	
	public static Cache cache;
	
	static Logger logger = Logger.getLogger(DataManager.class);
	
	
    /**
     * @param key
     * @return
     * @throws Exception
    */
	public static void saveTimeStamp(String key, LocalDateTime timeStamp) throws IOException {
		
		String fileTimeStampName = KVServer.storagePath+key+"-timestamp.txt";
        FileWriter fileWriter;
        File file = new File(fileTimeStampName);
        fileWriter = new FileWriter(fileTimeStampName);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
	    bufferedWriter.write(timeStamp.toString());
	    bufferedWriter.close();
        
	}
	
    /**
     * @param key
     * @return
     * @throws Exception
    */
	public static LocalDateTime getTimeStamp(String key) throws IOException {
		String fileTimeStampName = KVServer.storagePath+key+"-timestamp.txt";
        FileWriter fileWriter;
    	FileReader fileReader = new FileReader(fileTimeStampName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
    	String value = bufferedReader.readLine();
    	bufferedReader.close();
        return LocalDateTime.parse(value);
	}
	
	
    /**
     * @param key
     * @return
     * @throws FileNotFoundException 
     * @throws Exception
    */
	public static void deleteTimestamp(String key) throws FileNotFoundException {
		String fileTimeStampName = KVServer.storagePath+key+"-timestamp.txt";
    	File file = new File(fileTimeStampName);
    	if(!file.delete()) 
        { 
    		logger.error("Delete Exception ");
            throw new FileNotFoundException();
        } 
	}
	
    /**
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    public static StatusType put(String key, String value, boolean autoUpdateTimestamp) throws Exception {
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
	    if(autoUpdateTimestamp) {
	    	DataManager.saveTimeStamp(key, LocalDateTime.now());	
	    }
		cache.add(key, value);
		//Adding timeStamp
		
        return type;
    }
    
    
    public static File[] getAllTextFiles() {
    	File dir = new File(KVServer.storagePath);
    	logger.info("Storage Path: "+KVServer.storagePath);;
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
    		logger.error("Delete Exception ");
            throw new Exception();
        } 
    	deleteTimestamp(key);
    	if(cache.contains(key)) {
    		cache.delete(key);
    	}
    }

}
