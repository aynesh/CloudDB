package app_ecsServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import app_kvServer.KVServerAdminThread;
import app_kvServer.KVServerReplicationScheduler;
import app_kvServer.KVServerThread;

public class ECSReceiver {
	  
	static Logger logger = Logger.getLogger(ECSReceiver.class);
	
	public ECSReceiver(int port) {
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
        	logger.error("I/O error: " + e);
        }
        
        new ECSReceiverThread(socket).start();
    }
}
}
