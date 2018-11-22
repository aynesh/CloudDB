package app_kvServer;

import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

import common.messages.KVMessage;
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

	public boolean checkIfServerResponsible(String key) throws NoSuchAlgorithmException {
		return KVServer.metaData.getNode(key).getName().equals(this.nodeName) ? true : false;
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
				if (!KVServer.serveClients) {
					outMsg.setStatus(StatusType.SERVER_STOPPED);
				} else {
					switch (inpMsg.getStatus()) {
					case DELETE:
						try {
							if (this.checkIfServerResponsible(inpMsg.getKey())) {
								DataManager.delete(inpMsg.getKey());
								outMsg.setStatus(StatusType.DELETE_SUCCESS);
							} else {
								outMsg.setMetaData(KVServer.metaData.getMetaData());
								outMsg.setStatus(StatusType.SERVER_NOT_RESPONSIBLE);
							}

						} catch (Exception e) {

							outMsg.setStatus(StatusType.DELETE_ERROR);

						}
						break;

					case GET:
						try {
							if (this.checkIfServerResponsible(inpMsg.getKey())) {
								outMsg.setValue(DataManager.get(inpMsg.getKey()));
								outMsg.setStatus(StatusType.GET_SUCCESS);

							} else {
								outMsg.setMetaData(KVServer.metaData.getMetaData());
								outMsg.setStatus(StatusType.SERVER_NOT_RESPONSIBLE);
							}
						} catch (Exception e) {
							outMsg.setValue(e.getMessage());
							outMsg.setStatus(StatusType.GET_ERROR);
						}
						break;

					case PUT:
						try {
							if(KVServer.writeLock) {
								outMsg.setStatus(StatusType.SERVER_WRITE_LOCK);
							} else if (this.checkIfServerResponsible(inpMsg.getKey())) {
								outMsg.setStatus(DataManager.put(inpMsg.getKey(), inpMsg.getValue()));
								outMsg.setValue(inpMsg.getValue());
							} else {
								outMsg.setMetaData(KVServer.metaData.getMetaData());
								outMsg.setStatus(StatusType.SERVER_NOT_RESPONSIBLE);
							}

						} catch (Exception e) {
							outMsg.setValue(e.getMessage());
							outMsg.setStatus(StatusType.PUT_ERROR);
						}
						break;
					case TRANSFER:
						try {
							outMsg.setStatus(StatusType.TRANSFER_SUCCESS);
							DataManager.put(inpMsg.getKey(), inpMsg.getValue());
						} catch(Exception ex) {
							outMsg.setStatus(StatusType.TRANSFER_ERROR);
						}

					default:
						break;
					}
				}

				KVMessageManager.sendKVMessage(outMsg, out);
				// System.out.println(inpMsg.toString());
			} catch (Exception e) {

				return;
			}
		}
	}

}
