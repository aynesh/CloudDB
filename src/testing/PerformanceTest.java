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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import app.common.HashRing;
import app.common.Node;
import app_ecsServer.ECSServer;
import client.KVStore;
import common.messages.KVMessage;
import junit.framework.TestCase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PerformanceTest extends TestCase {
	
	public static volatile long totalPutTime=0;
	public static volatile long totalPutCount=0;
	public static volatile long totalGetTime=0;
	public static volatile long totalGetCount=0;
    public static final ConcurrentMap<String, AtomicLong> map = new ConcurrentHashMap<String, AtomicLong>();

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
		private HashRing metaData;
		SimulatedClient(HashRing ring) {
			metaData = ring;
		}
		
		public List<File> listOfFiles=new ArrayList<File>();
	    public void run() 
	    { 
	        try
	        { 
	        	for(File currentFile:listOfFiles) {

					Node targetNode = metaData.getNode(currentFile.getName());
	        		KVStore kvStore = new KVStore(targetNode.getIpAddress(), Integer.parseInt(targetNode.getPort()));
	        		kvStore.connect();
	        		
	        		Instant start = Instant.now();
	        		//KVMessage responsePut = kvStore.put(currentFile.getName(),"TEST_STRING");
	        		KVMessage responsePut = kvStore.put(currentFile.getName(), new String(Files.readAllBytes(Paths.get(currentFile.getAbsolutePath())), StandardCharsets.UTF_8));
	        		Instant end = Instant.now();
	        		Duration timeElapsed = Duration.between(start, end);
	        		totalPutTime+=timeElapsed.toMillis();
					totalPutCount++;
	        		
					start = Instant.now();
					KVMessage responseGet =  kvStore.get(currentFile.getName());
	        		end = Instant.now();
	        		timeElapsed = Duration.between(start, end);
	        		totalGetTime+=timeElapsed.toMillis();
	        		totalGetCount++;
	        		
	        		kvStore.disconnect();
	        	    
	        		map.putIfAbsent(responsePut.getStatus().name(), new AtomicLong(0));
	        		map.putIfAbsent(responseGet.getStatus().name(), new AtomicLong(0));
	        	    map.get(responsePut.getStatus().name()).incrementAndGet();
	        	    map.get(responseGet.getStatus().name()).incrementAndGet();
	        	}
	        } 
	        catch (Exception e) 
	        { 
	            // Throwing an exception 
	        	map.putIfAbsent("Exception", new AtomicLong(0));
	        	map.get("Exception").incrementAndGet();
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
		String command="initService "+numberOfServers+" 10 LFU 3";
		ecsServer.initService(command.split(" "));
		Thread.sleep(5000);
		ecsServer.start();
		Thread.sleep(5000);
		int noOfClients = 25;
		SimulatedClient[] simulatedClients = new SimulatedClient[noOfClients];
		
		int i=0;
		for(i=0; i<noOfClients; i++) {
			simulatedClients[i] = new SimulatedClient(ecsServer.getActiveServers());
		}
			

		for(File file:returnSetOfFiles()) {
			simulatedClients[i++%noOfClients].listOfFiles.add(file);
		}

		for(i=0; i<noOfClients; i++) {
			simulatedClients[i].start();
		}
		
		//ecsServer.removeNode();
		for(i=0; i<noOfClients; i++) {
			simulatedClients[i].join();
		}
		//Thread.sleep(70000);
		
		ecsServer.shutdown("ecs.config");
		System.out.println("Reached here.... ");
		System.out.println("========Report===========");
		System.out.println("Number of Servers: "+numberOfServers+" Number of Clients: "+noOfClients);
		System.out.println("Total Put Time in ms: "+(totalPutTime ));
		System.out.println("Total Put Count: "+(totalPutCount ));
		
		System.out.println("Total Get Time in ms: "+(totalGetTime));
		System.out.println("Total Get Count: "+(totalGetCount));
		System.out.println(map.toString());
	}
	

}
	
