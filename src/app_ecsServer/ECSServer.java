package app_ecsServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import app_kvServer.KVServer;
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
    						new Thread(new Runnable() {
    						     @Override
    						     public void run() {
    						    	 //Please configure SSH  keys for ur system and set the path in ECSServerLibaray for now
    						    	 ECSServerLibrary.launchProcess("localhost", "50000");
    						    	 // Else comment the above code and uncomment the below code.
    						    	 //new KVServer(50000, 10, "LFU")
    						     }
    						}).start();
    						outMsg.setCommand(Command.INIT_SERVICE_SUCCESS);
    					} catch (Exception e) {
    						
    					}
    					break;
    				case START:
    					try {
    						KVAdminMessageImpl msg = new KVAdminMessageImpl();
    						msg.setCommand(KVAdminMessage.Command.START);
    						ECSServerLibrary.sendMessage(msg, "127.0.0.1", 3000);
    						outMsg.setCommand(Command.START_SUCCESS);
    					} catch (Exception e) {
    						
    					}
    					break;
       				case STOP:
    					try {
    						KVAdminMessageImpl msg = new KVAdminMessageImpl();
    						msg.setCommand(KVAdminMessage.Command.STOP);
    						ECSServerLibrary.sendMessage(msg, "127.0.0.1", 3000);
    						outMsg.setCommand(Command.STOP_SUCCESS);
    					} catch (Exception e) {
    						
    					}
    					break;
       				case SHUTDOWN:
    					try {
    						KVAdminMessageImpl msg = new KVAdminMessageImpl();
    						msg.setCommand(KVAdminMessage.Command.SHUTDOWN);
    						ECSServerLibrary.sendMessage(msg, "127.0.0.1", 3000);
    						outMsg.setCommand(Command.SHUTDOWN_SUCCESS);
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


