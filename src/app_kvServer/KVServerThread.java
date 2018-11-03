package app_kvServer;

import java.io.*;
import java.net.Socket;

import org.apache.log4j.Logger;

import common.messages.KVMessage;
import common.messages.KVMessage.StatusType;
import common.messages.KVMessageManager;
import common.messages.impl.KVMessageImpl;
import datastore.DataManager;

	
public class KVServerThread extends Thread {
	protected Socket socket;
	
	static Logger logger = Logger.getLogger(KVServerThread.class);

    public KVServerThread(Socket clientSocket) {
    	
        this.socket = clientSocket;
    }
    
    public void run() {
    	
        InputStream inp = null;
        BufferedReader brinp = null;
        DataOutputStream out = null;
        
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }
        
        while (true) {
            try {
            	KVMessage inpMsg = KVMessageManager.receiveKVMessage(inp);
            	KVMessage outMsg = new KVMessageImpl(); 
            	outMsg.setKey(inpMsg.getKey());
            	
            	switch(inpMsg.getStatus()) {
				case DELETE:
					try {
						DataManager.delete(inpMsg.getKey());
						outMsg.setStatus(StatusType.DELETE_SUCCESS);
					} catch (Exception e) {
			 
						outMsg.setStatus(StatusType.DELETE_ERROR);
						
					}
					break;
					
				case GET:
					try {
						outMsg.setValue(DataManager.get(inpMsg.getKey()));
						outMsg.setStatus(StatusType.GET_SUCCESS);
					} catch (Exception e) {
						outMsg.setStatus(StatusType.GET_ERROR);
					}
					break;

				case PUT:
					try {
						outMsg.setStatus(DataManager.put(inpMsg.getKey(),inpMsg.getValue()));
						
					} catch (Exception e) {
						outMsg.setStatus(StatusType.PUT_ERROR);
					}
					
				default:
					break;
            	}
            	KVMessageManager.sendKVMessage(outMsg, out);
                System.out.println(inpMsg.toString());
            } catch (IOException | ClassNotFoundException e) {
                
                return;
            }
        }
    }
	

}
