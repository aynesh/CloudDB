package app_ecsServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import common.messages.KVAdminMessage;
import common.messages.KVAdminMessage.Command;
import common.messages.KVAdminMessageManager;
import common.messages.impl.KVAdminMessageImpl;

public class ECSServer {

	
	public static void main(String[] args) {

	  	ServerSocket serverSocket = null;
        Socket socket = null;
        
        try {
            //serverSocket = new ServerSocket(Integer.parseInt(args[1]));
        	serverSocket = new ServerSocket(5000);
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            
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
                	KVAdminMessage inpMsg = KVAdminMessageManager.receiveKVAdminMessage(inp);
                	KVAdminMessageImpl outMsg = new KVAdminMessageImpl(); 
            
                	switch(inpMsg.getCommand()) {
    				case INIT_SERVICE:
    					try {
    						outMsg.setCommand(Command.INIT_SERVICE_SUCCESS);
    					} catch (Exception e) {
    						
    					}
    					break;
    					
    				default:
    					break;
                	}
                	KVAdminMessageManager.sendKVAdminMessage(outMsg, out);
                	System.out.println("Message sent successfull.");
                } catch (Exception e) {
                    break;
                }
            }
        }
	}

}


