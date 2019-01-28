package app_kvServer;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import app.common.HashRing;
import common.messages.KVMessage;
import datastore.DataManager;

public class KVServer {
	
	static Logger logger = Logger.getLogger(KVServer.class);
	
    public static volatile boolean serveClients=false;
    
    public static volatile HashRing metaData=new HashRing();
    
	public static volatile boolean writeLock=false;
	
	public static volatile String storagePath="./";
	
	public static volatile  ConcurrentLinkedQueue<KVMessage> queue = new ConcurrentLinkedQueue<KVMessage>();

	public static String ECSIP = "localhost";

	public static int ECSPort = 40000;
	
	public static int readConsistencyLevel;
	
	public static int writeConsistencyLevel;
	
	private final ScheduledExecutorService scheduler;
	
	public static volatile int  replicationFactor=2;
	
	public static String nodeName;

	public static int readStats = 0;

	public static int writeStats = 0;
	
    /**
     * Start KV Server at given port
     * @param nodeName: Node identifier
     *
     * @param port      given port for storage server to operate
     * @param adminPort Port using which admin messages are communicated.
     * @param cacheSize specifies how many key-value pairs the server is allowed
     *                  to keep in-memory
     * @param strategy  specifies the cache replacement strategy in case the cache
     *                  is full and there is a GET- or PUT-request on a key that is
     *                  currently not contained in the cache. Options are "FIFO", "LRU",
     *                  and "LFU".
     */
    public KVServer(String nodeName, int port, int adminPort, int cacheSize, String strategy, String path, int repFactor, int readConsistencyLevel, int writeConsistencyLevel) {
    	
    	
    	logger.info("Starting KV Server: "+nodeName);
    	
    	this.nodeName = nodeName;
    	
    	KVServer.readConsistencyLevel = readConsistencyLevel;
    	
    	KVServer.writeConsistencyLevel = writeConsistencyLevel;
    	
    	KVServer.storagePath = path;
    	
    	KVServer.replicationFactor = repFactor;
    	
    	logger.info("Replication Factor : "+ replicationFactor);
    	
		new KVServerAdmin(adminPort, nodeName).start();
    	
    	DataManager.cache = CacheManager.instantiateCache(strategy,cacheSize);
        
    	ServerSocket serverSocket = null;
        Socket socket = null;
        
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();

        }
        if(replicationFactor > 0) {
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(new KVServerReplicationScheduler(nodeName), 1, 1, TimeUnit.MINUTES);        	
        }

        
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
            	logger.error("I/O error: " + e);
            }
            
            new KVServerThread(socket, nodeName).start();
        }
    }
    
    public KVServer(String nodeName, int port, int adminPort, int cacheSize, String strategy, String path, int repFactor) {
    	this(nodeName, port, adminPort, cacheSize, strategy, path, repFactor, 2, 2);
    	    
    }
    

	public static void main(String[] args) throws IOException
	{
        MDC.put("process_id", ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
		new KVServer(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]),  Integer.parseInt(args[3]), args[4], args[5], Integer.parseInt(args[6])); 
	}
    
    
}
