package app_kvServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import app_ecsServer.HashRing;
import datastore.DataManager;

public class KVServer {

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
	
    public static volatile boolean serveClients=false;
    
    public static volatile HashRing metaData=new HashRing();
    
	public static volatile boolean writeLock=false;
	
	public static volatile String storagePath="./";
	
    public KVServer(String nodeName, int port, int adminPort, int cacheSize, String strategy, String path) {
    	
    	System.out.println("Starting KV Server: "+nodeName);
    	
    	KVServer.storagePath = path;
    	
		new KVServerAdminThread(adminPort, nodeName).start();
    	
    	DataManager.cache = CacheManager.instantiateCache(strategy,cacheSize);
        
    	ServerSocket serverSocket = null;
        Socket socket = null;
        
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            
            new KVServerThread(socket, nodeName).start();
        }
    }
    
	public static void main(String[] args) throws IOException
	{
		new KVServer(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]),  Integer.parseInt(args[3]), args[4], args[5]); 
	}
    
    
}
