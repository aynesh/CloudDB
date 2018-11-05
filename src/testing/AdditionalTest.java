package testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import client.KVStore;
import common.messages.KVMessage;
import common.messages.KVMessage.StatusType;
import junit.framework.TestCase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdditionalTest extends TestCase {

    // TODO add your test cases, at least 3

    private KVStore kvClient;

    public void setUp() {
        kvClient = new KVStore("localhost", 50000);
        try {
            kvClient.connect();
        } catch (Exception e) {
        }
    }

    public void tearDown() {
        kvClient.disconnect();
    }

    @Test
    public void testKeyPresentFileSystem() {
    	long keyValue= System.currentTimeMillis();
        String key = Long.toString(keyValue);
        String value = "bar";
        KVMessage response = null;
        Exception ex = null;

        try {
            response = kvClient.put(key, value);
        } catch (Exception e) {
            ex = e;
        }
       	File file = new File(keyValue+".txt");
       	assertTrue(file.exists());
        assertTrue(ex == null && response.getStatus() == StatusType.PUT_SUCCESS);
    }
    
    
    @Test
    public void testKeyPresentInCaseOfUpdateInFileSystem() throws IOException {
    	long keyValue= System.currentTimeMillis();
        String key = Long.toString(keyValue);
        String updatedValue = "updated";
        String value = "bar";
        KVMessage response = null;
        Exception ex = null;

        try {
            response = kvClient.put(key, value);
            response = kvClient.put(key, updatedValue);
        } catch (Exception e) {
            ex = e;
        }
    	FileReader fileReader = new FileReader(keyValue+".txt");
    	BufferedReader bufferedReader = new BufferedReader(fileReader);
    	String valueFromFile = bufferedReader.readLine();
    	bufferedReader.close();
    	assertEquals(updatedValue, valueFromFile);
        assertTrue(ex == null && response.getStatus() == StatusType.PUT_UPDATE);
    }
    
    @Test
    public void testKeyNotPresentFileSystem() {
    	long keyValue= System.currentTimeMillis();
        String key = Long.toString(keyValue);
        String value = "bar";
        KVMessage response = null;
        Exception ex = null;

        try {
            response = kvClient.put(key, value);
            response = kvClient.put(key, "null");
        } catch (Exception e) {
            ex = e;
        }
       	assertFalse(new File(keyValue+".txt").isFile());
        assertTrue(ex == null && response.getStatus() == StatusType.DELETE_SUCCESS);
    }
    
}
