package app_kvServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import app.common.HashRing;
import app.common.Node;
import client.KVStore;
import common.messages.KVAdminMessage;
import common.messages.KVAdminMessage.Command;
import common.messages.KVAdminMessageManager;
import common.messages.KVMessage;
import common.messages.KVMessage.StatusType;
import common.messages.impl.KVAdminMessageImpl;
import datastore.DataManager;

public class KVServerAdminThread extends Thread {
	
	static Logger logger = Logger.getLogger(KVServerAdminThread.class);
	
	private int port = 3000;
	private String nodeName = "";
	Socket socket = null;
	
	public KVServerAdminThread(Socket socket, int port, String nodeName) {
		
		this.port = port;
		this.nodeName = nodeName;
		this.socket = socket;
	}
	
	public static synchronized void deleteFiles(Node ofNode,  HashRing metaData) {
		logger.info("Delete Replicated Files Started. Deleting ofNode: " + ofNode.getName());
		File files[] = DataManager.getAllTextFiles();
		
		for(File file: files) {
			String name = file.getName();

			String key = name.split(".txt")[0];
			logger.info("key: "+key+" Filename>"+name);
			try {
				if(metaData.getNode(key).getName().equals(ofNode.getName())) {
					DataManager.delete(key);
					logger.info("deleted : " +key);
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
		logger.info("Delete Replicated Files Completed.");
	}
	
	
	public static boolean isReplica(String key,String nodeName, Node toNode, HashRing metaData) throws NoSuchAlgorithmException {
		Node nextNode = metaData.getNextNode(nodeName);
		Node nextToNextNode = metaData.getNextNode(nextNode);
		if(nextNode != null && !nextNode.getName().equals(nodeName) && nodeName.equals(metaData.getNode(key).getName())) {
			return true;
		} else if(nextToNextNode != null && !nextToNextNode.getName().equals(nodeName) && nodeName.equals(metaData.getNode(key).getName())) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param nodeName The current node.
	 * @param toNode The node to transfer data to.
	 * @param metaData
	 * @param shutdownFlag true if its a removeNode command.
	 * @param replicationTransfer if true all files will be copied to target server without deleting from the server
	 */
	public static synchronized void transferData(String nodeName, Node toNode, HashRing metaData, boolean shutdownFlag, boolean replicationTransfer) {
		File files[] = DataManager.getAllTextFiles();
		List<String> keysToBeDeleted=new ArrayList<>();
		logger.info("Starting KV Server transfer-------");

		for(File file: files) {
			String name = file.getName();

			String key = name.split(".txt")[0];
			logger.info("key: "+key+" Filename>"+name);
			
			try {
				if(metaData.getNode(key).getName().equals(toNode.getName()) || (replicationTransfer && isReplica(key, nodeName, toNode, metaData))) {
					logger.info("Transfering Key: "+metaData.getNode(key).getName()+" fromNode: "+nodeName+" toNode: "+toNode.getName() + " replication: "+ replicationTransfer);
					
					try {
						String data=DataManager.get(key);
						KVStore kvClient = new KVStore(toNode.getIpAddress(), Integer.parseInt(toNode.getPort()));
						kvClient.connect();
						KVMessage msg = kvClient.transfer(key, data);
						if(msg.getStatus()==StatusType.COPY_SUCCESS && !replicationTransfer) {
							keysToBeDeleted.add(key);
							logger.info("Transfered: "+key);
						}
						kvClient.disconnect();
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						logger.error(e);
					}
				}
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if(!replicationTransfer) {
			logger.info("Delete Started....");
			for(String key: keysToBeDeleted) {
				try {
					DataManager.delete(key);
					logger.debug("Deleted key:"+key);
				} catch (Exception e) {
					logger.error("Delete failed");
				}
			}
			logger.info("Delete Completed....");
		}

		logger.info("Ending KV Server transfer-------");
		if(shutdownFlag) {
			System.exit(0);
		}
	}
	
	public void run() {
		/*ServerSocket serverSocket = null;
		Socket socket = null;
		try {
			serverSocket = new ServerSocket(this.port);
		} catch (IOException e) {
			e.printStackTrace();

		}
		while (true) {
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				logger.error("I/O error: " + e);
			}
*/
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
					logger.debug("Received Admin Command: "+inpMsg.toString());
					switch (inpMsg.getCommand()) {
					case PING_FORWARD:
						logger.info("Received failure detection message at "+nodeName);
						outMsg.setCommand(Command.PING_SUCCESS);
						break;
					case PING:
						outMsg.setCommand(Command.PING_SUCCESS);
						KVServer.ECSIP = inpMsg.getECSIP();
						KVServer.ECSPort = inpMsg.getPort();
						break;
					case START:
						try {
							KVServer.ECSIP = inpMsg.getECSIP();
							KVServer.ECSPort = inpMsg.getPort();
							KVServer.serveClients = true;
							KVServer.metaData.clearAndSetMetaData(inpMsg.getMetaData());
							outMsg.setCommand(Command.START_SUCCESS);
						} catch (Exception e) {
							outMsg.setCommand(Command.EXCEPTION);
						}
						logger.info("Started Accepting commands");
						break;
					case STOP:
						try {
							KVServer.serveClients = false;
							outMsg.setCommand(Command.STOP_SUCCESS);
						} catch (Exception e) {
							outMsg.setCommand(Command.EXCEPTION);
						}
						logger.info("Stopped Accepting commands");
						break;
					case SHUTDOWN:
						try {
							KVServer.serveClients = false;
							outMsg.setCommand(Command.SHUTDOWN_SUCCESS);
						} catch (Exception e) {
							outMsg.setCommand(Command.EXCEPTION);
						}
						logger.info("Shutdown initiated !");
						break;
					case SERVER_WRITE_LOCK:
						KVServer.writeLock = true;
						outMsg.setCommand(Command.SERVER_WRITE_LOCK);
						break;
					case SERVER_WRITE_UNLOCK:
						KVServer.writeLock = false;
						outMsg.setCommand(Command.SERVER_WRITE_UNLOCK);
						break;
					case TRANSFER:
					case TRANSFER_AND_SHUTDOWN:
						final boolean shutdownFlag;
						if(inpMsg.getCommand()==Command.TRANSFER_AND_SHUTDOWN) {
							shutdownFlag=true;
						} else {
							shutdownFlag=false;
						}
						KVServer.metaData.clearAndSetMetaData(inpMsg.getMetaData());
						new Thread(new Runnable() {
						     @Override
						     public void run() {
						    	 KVServerAdminThread.transferData(
						    			 nodeName, 
						    			 inpMsg.getTransferServer(), 
						    			 KVServer.metaData,
						    			 shutdownFlag, false);
						     }
						}).start();
						outMsg.setCommand(Command.TRANSFER_SUCCESS);
						break;
					case REPLICATE:
						new Thread(new Runnable() {
						     @Override
						     public void run() {
						    	 KVServerAdminThread.transferData(
						    			 nodeName, 
						    			 inpMsg.getTransferServer(), 
						    			 KVServer.metaData,
						    			 false, true);
						     }
						}).start();
						outMsg.setCommand(Command.REPLICATE_SUCCESS);				
						break;
					case DELETE_REPLICATED_FILES:
						new Thread(new Runnable() {
						     @Override
						     public void run() {
						    	 KVServerAdminThread.deleteFiles(
						    			 inpMsg.getTransferServer(), 
						    			 KVServer.metaData);
						     }
						}).start();
						outMsg.setCommand(Command.DELETE_REPLICATED_FILES_SUCCESS);				
						break;
					case META_DATA_UPDATE:
						KVServer.metaData.clearAndSetMetaData(inpMsg.getMetaData());
						outMsg.setCommand(Command.META_DATA_UPDATE_SUCCESS);
						break;
					default:
						break;
					}
					KVAdminMessageManager.sendKVAdminMessage(outMsg, out);
					if(inpMsg.getCommand()==Command.SHUTDOWN) {
						logger.info("Shutdown In Progress !");
						System.exit(0);
					}
					
					if(inpMsg.getCommand()==Command.PING_FORWARD) {
						logger.info("next->node "+KVServer.metaData.getNextNode(inpMsg.getServer()).getName()+" first node-> "+KVServer.metaData.getMetaData()[0].getName());
						Node nextNode = KVServer.metaData.getNextNode(inpMsg.getServer());
						if(nextNode.getName()==KVServer.metaData.getMetaData()[0].getName())
						{
							sendToECS(inpMsg.getServer(),Command.PING_SUCCESS);
						}
						else {
							if(!pingForward(nextNode)) {
								logger.info(nextNode.getName()+" is down!");
								reportFailure(nextNode);
							}
						}
							
					}
				} catch (Exception e) {
					break;
				}
			}

		}

	private boolean pingForward(Node toNode) {
		String ip = toNode.getIpAddress();
		int port = Integer.parseInt(toNode.getAdminPort());
		Socket sock;
		try {
			sock = new Socket(ip, port);
		} catch (UnknownHostException e) {
			return false;
			
		} catch (IOException e) {
			return false;
		}
		
		KVAdminMessage outMsg = new KVAdminMessageImpl();
		outMsg.setCommand(Command.PING_FORWARD);
		outMsg.setServer(toNode);
		
		try {
			InputStream in = sock.getInputStream();
			OutputStream out = sock.getOutputStream();
			logger.info("Sending failure detection message to "+toNode.getName());
			KVAdminMessageManager.sendKVAdminMessage(outMsg, out);
			logger.info("Waiting for response message from "+toNode.getName());
			KVAdminMessage inMsg = KVAdminMessageManager.receiveKVAdminMessage(in);
			
			sock.close();
			if(inMsg.getCommand()==Command.PING_SUCCESS) {
				logger.info(toNode.getName()+" is alive!");
				return true;
			}
		} catch (IOException|ClassNotFoundException e) {
		
			
		}
		
		return false;
		
	}
	
	
	private void reportFailure(Node toNode) {
		sendToECS(toNode, Command.PING_FAILURE);
	}
	
	private void sendToECS(Node toNode, Command cmd) {
		String ip = KVServer.ECSIP;
		int port = KVServer.ECSPort;
		Socket sock;
		try {
			sock = new Socket(ip, port);
		} catch (UnknownHostException e) {
			logger.error("Unknownhost "+ip+" "+port);
			return;
			
		} catch (IOException e) {
			logger.error("I/O exception "+ip+" "+port);
			return;
		}
		
		KVAdminMessage outMsg = new KVAdminMessageImpl();
		outMsg.setCommand(cmd);
		outMsg.setServer(toNode);
		try {
			InputStream in = sock.getInputStream();
			OutputStream out = sock.getOutputStream();
			logger.info("Reporting to ECS: "+ip+" "+port);
			KVAdminMessageManager.sendKVAdminMessage(outMsg, out);
			sock.close();
			
		} catch (IOException e) {
			logger.error("I/O exception "+ip+" "+port);
		}
		
	}
}
