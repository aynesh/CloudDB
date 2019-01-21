package app_kvServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

import app.common.Node;
import common.messages.KVMessage;
import common.messages.KVMessage.DataType;
import common.messages.KVMessage.StatusType;
import common.messages.KVMessageManager;
import common.messages.impl.KVMessageImpl;
import datastore.DataManager;

public class KVServerThread extends Thread {
	protected Socket socket;

	private String nodeName;

	static Logger logger = Logger.getLogger(KVServerThread.class);

	public KVServerThread(Socket clientSocket, String name) {

		this.socket = clientSocket;
		this.nodeName = name;

	}
	
	public boolean checkIfReplica(String key) throws NoSuchAlgorithmException {
		Node prevNode = KVServer.metaData.getPrevNode(this.nodeName);
		Node prevNode2 = null;
		if(prevNode != null) {
			prevNode2 = KVServer.metaData.getPrevNode(prevNode);
		}
		if(prevNode != null && KVServer.metaData.getNode(key).getName().equals(prevNode.getName())) {
			return true; 
		}
		if(prevNode2 != null  && KVServer.metaData.getNode(key).getName().equals(prevNode2.getName())) {
			return true; 
		}
		return false;
	}

	public boolean checkIfServerResponsible(String key) throws NoSuchAlgorithmException {
		return KVServer.metaData.getNode(key).getName().equals(this.nodeName) ? true : false;
	}
	
	public void queueReplication(KVMessage inMessage, StatusType operationResult) {
		KVMessage outMessage = new KVMessageImpl();
		outMessage.setKey(inMessage.getKey());
		switch(operationResult) {
			case DELETE_SUCCESS: 
				outMessage.setStatus(StatusType.DELETE_REPLICA_COPY);
				break;
			case PUT_SUCCESS:
			case PUT_UPDATE:
				outMessage.setStatus(StatusType.COPY);
				outMessage.setValue(inMessage.getValue());
				break;
			default: return;
		}
		outMessage.setDataType(DataType.REPLICA_COPY);
		KVServer.queue.add(outMessage);
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
				if (!KVServer.serveClients && (inpMsg.getStatus() != StatusType.COPY)) {
					outMsg.setStatus(StatusType.SERVER_STOPPED);
				} else {
					switch (inpMsg.getStatus()) {
					case DELETE:
						try {
							if (this.checkIfServerResponsible(inpMsg.getKey())) {
								DataManager.delete(inpMsg.getKey());
								outMsg.setStatus(StatusType.DELETE_SUCCESS);
								outMsg.setMetaData(KVServer.metaData.getMetaData());
								queueReplication(inpMsg,StatusType.DELETE_SUCCESS);
							} else {
								outMsg.setMetaData(KVServer.metaData.getMetaData());
								outMsg.setStatus(StatusType.SERVER_NOT_RESPONSIBLE);
							}

						} catch (Exception e) {
							outMsg.setMetaData(KVServer.metaData.getMetaData());
							outMsg.setStatus(StatusType.DELETE_ERROR);

						}
						break;

					case GET:
						try {
							if (this.checkIfServerResponsible(inpMsg.getKey()) || this.checkIfReplica(inpMsg.getKey())) {
								outMsg.setValue(DataManager.get(inpMsg.getKey()));
								outMsg.setStatus(StatusType.GET_SUCCESS);
								outMsg.setMetaData(KVServer.metaData.getMetaData());

							} else {
								outMsg.setMetaData(KVServer.metaData.getMetaData());
								outMsg.setStatus(StatusType.SERVER_NOT_RESPONSIBLE);
							}
						} catch (Exception e) {
							outMsg.setMetaData(KVServer.metaData.getMetaData());
							outMsg.setValue(e.getMessage());
							if( (e instanceof FileNotFoundException) && this.checkIfReplica(inpMsg.getKey())) {
								outMsg.setStatus(StatusType.REPLICA_NOT_AVAILABLE);
							} else {
								outMsg.setStatus(StatusType.GET_ERROR);
							}
						}
						break;

					case PUT:
						try {
							if(KVServer.writeLock) {
								outMsg.setStatus(StatusType.SERVER_WRITE_LOCK);
							} else if (this.checkIfServerResponsible(inpMsg.getKey())) {
								StatusType operationStatus = DataManager.put(inpMsg.getKey(), inpMsg.getValue());
								outMsg.setStatus(operationStatus);
								outMsg.setValue(inpMsg.getValue());
								outMsg.setMetaData(KVServer.metaData.getMetaData());
								queueReplication(inpMsg,operationStatus);
							} else {
								outMsg.setMetaData(KVServer.metaData.getMetaData());
								outMsg.setStatus(StatusType.SERVER_NOT_RESPONSIBLE);
							}

						} catch (Exception e) {
							outMsg.setMetaData(KVServer.metaData.getMetaData());
							outMsg.setValue(e.getMessage());
							outMsg.setStatus(StatusType.PUT_ERROR);
						}
						break;
					case COPY:
						try {
							outMsg.setStatus(StatusType.COPY_SUCCESS);
							DataManager.put(inpMsg.getKey(), inpMsg.getValue());
						} catch(Exception ex) {
							outMsg.setStatus(StatusType.COPY_ERROR);
							logger.error("Transfer Error: "+ex.toString());
						}
						break;
					case DELETE_REPLICA_COPY:
						try {
							outMsg.setStatus(StatusType.DELETE_REPLICA_COPY_SUCCESS);
							DataManager.delete(inpMsg.getKey());
						} catch(Exception ex) {
							outMsg.setStatus(StatusType.DELETE_REPLICA_COPY_ERROR);
							logger.error("Dekete Replica Copy Error: "+ex.toString());
						}
						break;
					default:
						break;
					}
				}

				KVMessageManager.sendKVMessage(outMsg, out);

			} catch (Exception e) {

				return;
			}
		}
	}

}
