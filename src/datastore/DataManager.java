package datastore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import common.messages.KVMessage.StatusType;

public class DataManager {
    public static StatusType put(String key, String value) throws Exception {
        String fileName = key+".txt";
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
		    
        return type;
    }

    public static String get(String key) throws Exception {
    	String fileName = key+".txt";
    	FileReader fileReader = new FileReader(fileName);
    	BufferedReader bufferedReader = new BufferedReader(fileReader);
    	String value = bufferedReader.readLine();
    	bufferedReader.close();
    	return value;
    }
    
    public static void delete(String key) throws Exception {
    	String fileName = key+".txt";
    	File file = new File(fileName);
    	if(!file.delete()) 
        { 
            throw new Exception();
        } 
    }

}
