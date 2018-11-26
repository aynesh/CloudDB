package app_kvServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
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

	public KVServerAdminThread(int port, String nodeName) {
		
		this.port = port;
		this.nodeName = nodeName;
	}
	
	/**
	 * @param nodeName The current node.
	 * @param toNode The node to transfer data to.
	 * @param metaData
	 * @param shutdownFlag true if its a removeNode command.
	 */
	public static void transferData(String nodeName, Node toNode, HashRing metaData, boolean shutdownFlag) {
		File files[] = DataManager.getAllTextFiles();
		List<String> keysToBeDeleted=new ArrayList<>();
		
		for(File file: files) {
			String name = file.getName();

			String key = name.split(".txt")[0];
			logger.info("key: "+key+" Filename>"+name);
			
			try {
				logger.info("Belongs to: "+metaData.getNode(key).getName()+" Checking toNode: "+toNode.getName());
				if(metaData.getNode(key).getName().equals(toNode.getName())) {
					try {
						String data=DataManager.get(key);
						KVStore kvClient = new KVStore(toNode.getIpAddress(), Integer.parseInt(toNode.getPort()));
						kvClient.connect();
						KVMessage msg = kvClient.transfer(key, data);
						if(msg.getStatus()==StatusType.TRANSFER_SUCCESS) {
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
		logger.info("Delete Started....");
		for(String key: keysToBeDeleted) {
			try {
				DataManager.delete(key);
			} catch (Exception e) {
				logger.error("Delete failed");
			}
		}
		logger.info("Delete Completed....");
		logger.info("Ending KV Server transfer-------");
		if(shutdownFlag) {
			System.exit(0);
		}
	}
	
	public void run() {
		ServerSocket serverSocket = null;
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

					switch (inpMsg.getCommand()) {
					case PING:
						outMsg.setCommand(Command.PING_SUCCESS);
						break;
					case START:
						try {
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
						    			 shutdownFlag);
						     }
						}).start();
						outMsg.setCommand(Command.TRANSFER_SUCCESS);
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
				} catch (Exception e) {
					break;
				}
			}

		}
	}
}
