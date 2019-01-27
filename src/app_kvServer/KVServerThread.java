package app_kvServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

import org.apache.log4j.Logger;

import app.common.Node;
import common.messages.KVMessage;
import common.messages.KVMessage.DataType;
import common.messages.KVMessage.StatusType;
import common.messages.KVMessageManager;
import common.messages.impl.KVMessageImpl;
import datastore.ConsistentDataManager;
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
		return KVServer.metaData.checkIfReplica(key, KVServer.metaData.getNodeObject(this.nodeName), KVServer.replicationFactor);
	}


	public boolean checkIfServerResponsible(String key) throws NoSuchAlgorithmException {
		return KVServer.metaData.getNode(key).getName().equals(this.nodeName) ? true : false;
	}
	
	public void queueReplication(KVMessage inMessage, StatusType operationResult) throws IOException {
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
				outMessage.setTimestamp(DataManager.getTimeStamp(inMessage.getKey()));
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
			logger.info("IOException1: "+e.getMessage());
			return;
		}

		while (true) {
			try {
				KVMessage inpMsg = KVMessageManager.receiveKVMessage(inp);
				KVMessage outMsg = new KVMessageImpl();
				outMsg.setKey(inpMsg.getKey());
				if (!KVServer.serveClients && (inpMsg.getStatus() != StatusType.COPY 
						&& inpMsg.getStatus() != StatusType.COPY_AND_REPLICATE
						&& inpMsg.getStatus() != StatusType.DELETE_REPLICA_COPY
						)) {
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
							logger.info("DELETE_ERROR: "+e.getClass()+" "+e.getMessage());
							outMsg.setMetaData(KVServer.metaData.getMetaData());
							outMsg.setStatus(StatusType.DELETE_ERROR);

						}
						break;

					case GET:
						KVServer.readStats++;
						try {
							if (this.checkIfServerResponsible(inpMsg.getKey()) || this.checkIfReplica(inpMsg.getKey())) {
								String responseValue = ConsistentDataManager.get(inpMsg.getKey());
								outMsg.setValue(responseValue);
								outMsg.setStatus(StatusType.GET_SUCCESS);
								outMsg.setMetaData(KVServer.metaData.getMetaData());
								outMsg.setTimestamp(DataManager.getTimeStamp(inpMsg.getKey()));
							} else {
								outMsg.setMetaData(KVServer.metaData.getMetaData());
								outMsg.setStatus(StatusType.SERVER_NOT_RESPONSIBLE);
							}
						} catch (Exception e) {
							logger.warn("GET_ERROR_WARN", e);
							logger.info("GET_ERROR: "+e.getClass()+" "+e.getMessage());
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
						KVServer.writeStats++;
						try {
							if(KVServer.writeLock) {
								outMsg.setStatus(StatusType.SERVER_WRITE_LOCK);
							} else if (this.checkIfServerResponsible(inpMsg.getKey())) {
								StatusType operationStatus = ConsistentDataManager.put(inpMsg.getKey(), inpMsg.getValue());
								outMsg.setStatus(operationStatus);
								outMsg.setValue(inpMsg.getValue());
								outMsg.setMetaData(KVServer.metaData.getMetaData());
								queueReplication(inpMsg,operationStatus);
							} else {
								outMsg.setMetaData(KVServer.metaData.getMetaData());
								outMsg.setStatus(StatusType.SERVER_NOT_RESPONSIBLE);
							}

						} catch (Exception e) {
							logger.info("PUT_ERROR"+e.getClass()+" "+e.getMessage());
							outMsg.setMetaData(KVServer.metaData.getMetaData());
							outMsg.setValue(e.getMessage());
							outMsg.setStatus(StatusType.PUT_ERROR);
						}
						break;
						
					case COPY_AND_REPLICATE:
					case COPY:
						try {
							logger.info("isReplica: "+this.checkIfReplica(inpMsg.getKey()));
							logger.info("isResponsible: "+this.checkIfServerResponsible(inpMsg.getKey()));
							if(this.checkIfReplica(inpMsg.getKey()) || this.checkIfServerResponsible(inpMsg.getKey())) {
								outMsg.setStatus(StatusType.COPY_SUCCESS);
								DataManager.put(inpMsg.getKey(), inpMsg.getValue(), false, LocalDateTime.now());
								DataManager.saveTimeStamp(inpMsg.getKey(), inpMsg.getTimestamp());
								if(inpMsg.getStatus()== StatusType.COPY_AND_REPLICATE) {
									queueReplication(inpMsg,StatusType.PUT_SUCCESS);
								}
							} else {
								outMsg.setStatus(StatusType.COPY_ERROR);
								logger.info("transfer: Cannot accept. Neither replica nor responsible server.");
							}


						} catch(Exception ex) {
							logger.info("COPY_ERROR: "+ex.getClass()+" "+ex.getMessage());
							outMsg.setStatus(StatusType.COPY_ERROR);
						}
						break;
					case DELETE_REPLICA_COPY:
						try {
							outMsg.setStatus(StatusType.DELETE_REPLICA_COPY_SUCCESS);
							DataManager.delete(inpMsg.getKey());
						} catch(Exception ex) {
							logger.info("DELETE_REPLICA_COPY_ERROR: "+ex.getClass()+" "+ex.getMessage());
							outMsg.setStatus(StatusType.DELETE_REPLICA_COPY_ERROR);
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
