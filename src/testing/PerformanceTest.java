package testing;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import app_ecsServer.ECSServer;
import app_kvClient.KVClient;
import junit.framework.TestCase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PerformanceTest extends TestCase {
	
	public static volatile long totalPutTime=0;
	public static volatile long totalPutCount=0;
	public static volatile long totalGetTime=0;
	public static volatile long totalGetCount=0;
	
	public long runGet(KVClient kvClient, String key) throws Exception {
		Instant start = Instant.now();
		kvClient.Get(key);
		Instant end = Instant.now();
		Duration timeElapsed = Duration.between(start, end);
		return timeElapsed.toMillis();
	}

	public long runPut(KVClient kvClient, String key, String value) throws Exception {
		Instant start = Instant.now();
		kvClient.Put(key, value);
		Instant end = Instant.now();
		Duration timeElapsed = Duration.between(start, end);
		return timeElapsed.toMillis();
	}
	
	public File[] returnSetOfFiles() {
    	File dir = new File("/home/aynesh/data_set");
        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
        });
	}
	
	
	
	class SimulatedClient extends Thread 
	{ 
		
		public List<File> listOfFiles=new ArrayList<File>();
	    public void run() 
	    { 
	        try
	        { 
	        	for(File currentFile:listOfFiles) {
					KVClient kvClient=new KVClient();
					String kvCommand="connect 127.0.0.1 50003";
					kvClient.Connect(kvCommand.split(" "));
					String fileText = new String(Files.readAllBytes(Paths.get(currentFile.getAbsolutePath())), StandardCharsets.UTF_8);
					totalPutTime+=runPut(kvClient, currentFile.getName(), fileText);
					totalPutCount++;
					kvClient.Disconnect();
					kvClient.Connect(kvCommand.split(" "));
					totalGetTime+=runGet(kvClient, currentFile.getName());
					kvClient.Disconnect();
					totalGetCount++;
	        	}
	        } 
	        catch (Exception e) 
	        { 
	            // Throwing an exception 
	            System.out.println ("Exception is caught"); 
	        } 
	    } 
	} 
	
	@Test
	public void test5Servers() throws Exception {
		ECSServer ecsServer =  new ECSServer();
		ecsServer.initializeActiveServers();
		ecsServer.initializeServerConfig("ecs.config");
		int numberOfServers=5;
		String command="initService "+numberOfServers+" 10 LFU";
		ecsServer.initService(command.split(" "));
		Thread.sleep(5000);
		ecsServer.start();
		Thread.sleep(5000);
		int noOfClients = 1;
		SimulatedClient[] simulatedClients = new SimulatedClient[noOfClients];
		
		int i=0;
		for(i=0; i<noOfClients; i++) {
			simulatedClients[i] = new SimulatedClient();
		}
			

		for(File file:returnSetOfFiles()) {
			simulatedClients[i++%noOfClients].listOfFiles.add(file);
		}

		for(i=0; i<noOfClients; i++) {
			simulatedClients[i].start();
		}
		

		for(i=0; i<noOfClients; i++) {
			simulatedClients[i].join();
		}
		Thread.sleep(70000);
		
		ecsServer.shutdown("ecs.config");
		System.out.println("Reached here.... ");
		System.out.println("========Report===========");
		System.out.println("Number of Servers: "+numberOfServers+" Number of Clients: "+noOfClients);
		System.out.println("Total Put Time in ms: "+(totalPutTime ));
		System.out.println("Total Put Count: "+(totalPutCount ));
		
		System.out.println("Total Get Time in ms: "+(totalGetTime));
		System.out.println("Total Get Count: "+(totalGetCount));
	}
	

}
	
