package app_ecsServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import common.messages.KVAdminMessage;
import common.messages.KVAdminMessage.Command;
import common.messages.KVAdminMessageManager;
import common.messages.impl.KVAdminMessageImpl;

public class ECSServer {
	
	public static HashRing activeServers = new HashRing();
	
	public static void launchServers(Map<String, Node> serverConfig, int cacheSize, String cacheStrategy, int numberOfServers) {
		int i=1;
		ArrayList<String> itemsToRemove = new ArrayList();
		for (Map.Entry<String, Node> item : serverConfig.entrySet())
		{
			if(i>numberOfServers)
				break;
			
			new Thread(new Runnable() {
			     @Override
			     public void run() {
			    	 //Please configure SSH  keys for ur system and set the path in ECSServerLibaray for now
			    	 Node node = item.getValue();
			    	 ECSServerLibrary.launchProcess(node.getName(), node.getIpAddress(), node.getUserName(), node.getLocation(), node.getPort(), node.getAdminPort(), cacheSize, cacheStrategy, node.getStoragePath() );
			    	 
			    	 // Else comment the above code and uncomment the below code.
			    	 //new KVServer(50000, 10, "LFU")
			     }
			}).start();
			
			activeServers.addNode(item.getValue());
			itemsToRemove.add(item.getKey());
			i++;
		}
		for(String key: itemsToRemove) {
			serverConfig.remove(key);
		}
		

	}
	
	
	
	public static void addNode( Map<String, Node> serverConfig, int cacheSize, String cacheStrategy) {
		int i=1;
		String keyToRemove=null;
		Node newNode=null;
		for (Map.Entry<String, Node> item : serverConfig.entrySet())
		{
			if(i>1)
				break;
			
			new Thread(new Runnable() {
			     @Override
			     public void run() {
			    	 Node node = item.getValue();
			    	 ECSServerLibrary.launchProcess(node.getName(), node.getIpAddress(), node.getUserName(), node.getLocation(), node.getPort(), node.getAdminPort(), cacheSize, cacheStrategy , node.getStoragePath());
			     }
			}).start();
			
			newNode = item.getValue();
			i++;
			
		}
		
		
			activeServers.addNode(newNode);
			keyToRemove = newNode.getName();
			
			Node prevNode = activeServers.getPrevNode(newNode);
			Node nextNode = activeServers.getNextNode(newNode);
			
			writeLockUnlockServers(newNode, prevNode, true);
			writeLockUnlockServers(newNode, nextNode, true);
			
			boolean serverOnline=false;
			KVAdminMessageImpl pingMessage =  new KVAdminMessageImpl();
			pingMessage.setCommand(Command.PING);
			while(!serverOnline) {
				KVAdminMessage reply = ECSServerLibrary.sendMessage(pingMessage, newNode.getIpAddress(), Integer.parseInt(newNode.getAdminPort()));
				if(reply!=null) {
					if(reply.getCommand() == Command.PING_SUCCESS) {
						serverOnline=true;
						System.out.println("Ping Success");
					}
				}
				try {
					System.out.println("Ping sleep");
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			initiateTransferFiles(newNode, prevNode, nextNode, activeServers.getMetaData());
			
			updateMetaData(newNode);
			updateMetaData(prevNode);
			updateMetaData(nextNode);
			
			writeLockUnlockServers(newNode, prevNode, false);
			writeLockUnlockServers(newNode, nextNode, false);
			
			//Transfer Keys


		serverConfig.remove(keyToRemove);

	}
	
	
	public static void updateMetaData(Node targetNode) {
		KVAdminMessageImpl msg = new KVAdminMessageImpl();
		msg.setCommand(Command.META_DATA_UPDATE);
		msg.setMetaData(activeServers.getMetaData());
		notifySingleServer(msg, targetNode);
	}
	
	public static void removeNode(Map<String, Node> serverConfig) {
		String key=null;
		Node selectedNode=null;
		Node nodes[]=activeServers.getMetaData();
		
		if(nodes.length == 0 ) {
			return;
		}
		selectedNode = nodes[0];
			
		Node nextNode = activeServers.getNextNode(selectedNode);
		Node prevNode = activeServers.getPrevNode(selectedNode);
		
		activeServers.removeNode(selectedNode);
		
		writeLockUnlockServers(selectedNode, selectedNode, true);
		writeLockUnlockServers(selectedNode, nextNode, true);
			
		initiateTransferFilesForRemove(selectedNode, prevNode, nextNode, activeServers.getMetaData());
		
		updateMetaData(nextNode);
			
		//writeLockUnlockServers(item.getValue(), item.getValue(), false); // Server Shutdown Not needed
		writeLockUnlockServers(selectedNode, nextNode, false);
			
		//Transfer Keys
		
		//Shutdwon is implmeneted at the KV Server itself
		serverConfig.put(selectedNode.getName(), selectedNode);
	}
	
	public static void writeLockUnlockServers(Node referenceNode, Node targetNode, boolean lock) {
		KVAdminMessageImpl msg = new KVAdminMessageImpl();
		msg.setCommand(lock ? KVAdminMessage.Command.SERVER_WRITE_LOCK : KVAdminMessage.Command.SERVER_WRITE_UNLOCK);
		if( !targetNode.getName().equals(referenceNode.getName())) {
			notifySingleServer(msg, targetNode);
		}
	}
	
	//Repeated can be optimized
	public static void initiateTransferFilesForRemove(Node currentNode, Node prevNode, Node nextNode, Node[] metaData ) {
		KVAdminMessageImpl msg = new KVAdminMessageImpl();
		msg.setCommand(Command.TRANSFER_AND_SHUTDOWN); // Below is a blocking operation !
		msg.setMetaData(metaData);
		if( !nextNode.getName().equals(currentNode.getName())) {
			msg.setTransferStartKey(prevNode.getEndRange());
			msg.setTransferEndKey(currentNode.getEndRange());
			msg.setTransferServer(nextNode);
			notifySingleServer(msg, currentNode);
		}
	}
	
	public static void initiateTransferFiles(Node newNode, Node prevNode, Node nextNode, Node[] metaData) {
		KVAdminMessageImpl msg = new KVAdminMessageImpl();
		msg.setCommand(Command.TRANSFER); // Below is a blocking operation !
		msg.setMetaData(metaData);
		if( !prevNode.getName().equals(newNode.getName())) {
			msg.setTransferServer(newNode);
			notifySingleServer(msg, prevNode);
		}
		if( !nextNode.getName().equals(newNode.getName())) {
			msg.setTransferServer(newNode);
			notifySingleServer(msg, nextNode);
		}
	}
	
	public static void notifyAllServers(KVAdminMessage msg) {
		for (Node node : ECSServer.activeServers.getMetaData())
		{
			ECSServerLibrary.sendMessage(msg, node.getIpAddress(), Integer.parseInt(node.getAdminPort()));
		}
	}
	
	public static void notifySingleServer(KVAdminMessage msg, Node node) {
		ECSServerLibrary.sendMessage(msg, node.getIpAddress(), Integer.parseInt(node.getAdminPort()));
	}

	
	public static void main(String[] args) {
		Map<String, Node> serverConfig = ECSServerLibrary.readConfigFile();

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
    						KVAdminMessageImpl adminMsg = new KVAdminMessageImpl();
    						adminMsg.setCommand(KVAdminMessage.Command.START);
    						adminMsg.setMetaData(ECSServer.activeServers.getMetaData());
    						ECSServer.notifyAllServers(adminMsg);
    						outMsg.setCommand(Command.START_SUCCESS);
    					} catch (Exception e) {
    						
    					}
    					break;
       				case STOP:
    					try {
    						KVAdminMessageImpl adminMsg = new KVAdminMessageImpl();
    						adminMsg.setCommand(KVAdminMessage.Command.STOP);
    						ECSServer.notifyAllServers(adminMsg);
    						outMsg.setCommand(Command.STOP_SUCCESS);
    					} catch (Exception e) {
    						
    					}
    					break;
       				case SHUTDOWN:
    					try {
    						KVAdminMessageImpl adminMsg = new KVAdminMessageImpl();
    						adminMsg.setCommand(KVAdminMessage.Command.SHUTDOWN);
    						ECSServer.notifyAllServers(adminMsg);
    						ECSServer.activeServers.removeAll();
    						outMsg.setCommand(Command.SHUTDOWN_SUCCESS);
    						serverConfig = ECSServerLibrary.readConfigFile(); // Reinitalize Server Config
    					} catch (Exception e) {
    						
    					}
    					break;
       				case ADD_NODE:
    					try {
    						
    						addNode(serverConfig, inpMsg.getCacheSize(), inpMsg.getCacheType());
    						//ECSServer.launchServers(serverConfig, inpMsg.getCacheSize(), inpMsg.getCacheType(), 1 );
    						outMsg.setMetaData(ECSServer.activeServers.getMetaData());
    						outMsg.setCommand(Command.ADD_NODE_SUCCESS);
    					} catch (Exception e) {
    						outMsg.setCommand(Command.EXCEPTION);
    					}
       					break;
       				case REMOVE_NODE:
    					try {
    						removeNode(serverConfig);
    						outMsg.setMetaData(activeServers.getMetaData());
    						outMsg.setCommand(Command.REMOVE_NODE_SUCCESS);
    					} catch (Exception e) {
    						outMsg.setCommand(Command.EXCEPTION);
    					}
       					break;
       				case GET_META_DATA:
       					outMsg.setMetaData(activeServers.getMetaData());
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


