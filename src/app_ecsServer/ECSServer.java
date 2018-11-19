package app_ecsServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import common.messages.KVAdminMessage;
import common.messages.KVAdminMessage.Command;
import common.messages.KVAdminMessageManager;
import common.messages.impl.KVAdminMessageImpl;

public class ECSServer {
	
	public static HashRing activeServers = new HashRing();
	
	public static void launchServers(Map<String, Node> serverConfig, int cacheSize, String cacheStrategy, int numberOfServers) {
		int i=1;
		for (Map.Entry<String, Node> item : serverConfig.entrySet())
		{
			if(i>numberOfServers)
				break;
			
			new Thread(new Runnable() {
			     @Override
			     public void run() {
			    	 //Please configure SSH  keys for ur system and set the path in ECSServerLibaray for now
			    	 Node node = item.getValue();
			    	 ECSServerLibrary.launchProcess(node.getName(), node.getIpAddress(), node.getUserName(), node.getLocation(), node.getPort(), node.getAdminPort(), cacheSize, cacheStrategy );
			    	 
			    	 // Else comment the above code and uncomment the below code.
			    	 //new KVServer(50000, 10, "LFU")
			     }
			}).start();
			
			activeServers.addNode(item.getValue());
			i++;
		}

	}
	
	public static void notifyAllServers(KVAdminMessage msg) {
		for (Node node : ECSServer.activeServers.getMetaData())
		{
			ECSServerLibrary.sendMessage(msg, node.getIpAddress(), Integer.parseInt(node.getAdminPort()));
		}
	}

	
	public static void main(String[] args) {
		
		final Map<String, Node> serverConfig = ECSServerLibrary.readConfigFile();

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
    						if(inpMsg.getNumberOfNodes() > serverConfig.size()) {
    							outMsg.setCommand(Command.INIT_SERVICE_FAIL);
    						} else {
    							ECSServer.launchServers(serverConfig, inpMsg.getCacheSize(), inpMsg.getCacheType(), inpMsg.getNumberOfNodes() );
    						}

    						outMsg.setCommand(Command.INIT_SERVICE_SUCCESS);
    					} catch (Exception e) {
    						
    					}
    					break;
    				case START:
    					try {
    						KVAdminMessageImpl msg = new KVAdminMessageImpl();
    						msg.setCommand(KVAdminMessage.Command.START);
    						msg.setMetaData(ECSServer.activeServers.getMetaData());
    						ECSServer.notifyAllServers(msg);
    						outMsg.setCommand(Command.START_SUCCESS);
    					} catch (Exception e) {
    						
    					}
    					break;
       				case STOP:
    					try {
    						KVAdminMessageImpl msg = new KVAdminMessageImpl();
    						msg.setCommand(KVAdminMessage.Command.STOP);
    						ECSServer.notifyAllServers(msg);
    						outMsg.setCommand(Command.STOP_SUCCESS);
    					} catch (Exception e) {
    						
    					}
    					break;
       				case SHUTDOWN:
    					try {
    						KVAdminMessageImpl msg = new KVAdminMessageImpl();
    						msg.setCommand(KVAdminMessage.Command.SHUTDOWN);
    						ECSServer.notifyAllServers(msg);
    						ECSServer.activeServers.removeAll();
    						outMsg.setCommand(Command.SHUTDOWN_SUCCESS);
    					} catch (Exception e) {
    						
    					}
    					break;
    				default:
    					break;
                	}
                	KVAdminMessageManager.sendKVAdminMessage(outMsg, out);
                } catch (Exception e) {
                	System.out.println(e);
                    break;
                }
            }
        }
	}

}


