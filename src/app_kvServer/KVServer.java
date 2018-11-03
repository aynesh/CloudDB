package app_kvServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import datastore.DataManager;

public class KVServer {

    /**
     * Start KV Server at given port
     *
     * @param port      given port for storage server to operate
     * @param cacheSize specifies how many key-value pairs the server is allowed
     *                  to keep in-memory
     * @param strategy  specifies the cache replacement strategy in case the cache
     *                  is full and there is a GET- or PUT-request on a key that is
     *                  currently not contained in the cache. Options are "FIFO", "LRU",
     *                  and "LFU".
     */
	
	
    public KVServer(int port, int cacheSize, String strategy) {
    	
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
            
            new KVServerThread(socket).start();
        }
    }
    
    
}
