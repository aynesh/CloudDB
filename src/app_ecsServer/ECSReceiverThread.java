package app_ecsServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

import app.common.Node;
import app_kvServer.KVServer;
import app_kvServer.KVServerThread;
import common.messages.KVAdminMessage;
import common.messages.KVAdminMessage.Command;
import common.messages.KVAdminMessageManager;
import common.messages.KVMessage;
import common.messages.KVMessageManager;
import common.messages.KVMessage.DataType;
import common.messages.KVMessage.StatusType;
import common.messages.impl.KVAdminMessageImpl;
import common.messages.impl.KVMessageImpl;
import datastore.DataManager;


 public class ECSReceiverThread extends Thread {
	protected Socket socket;

	private String nodeName;

	static Logger logger = Logger.getLogger(ECSReceiverThread.class);

	public ECSReceiverThread(Socket clientSocket) {

		this.socket = clientSocket;

	}
	
	public void run() {

		InputStream inp = null;
		
		DataOutputStream out = null;

		try {
			inp = socket.getInputStream();
			out = new DataOutputStream(socket.getOutputStream());
			
		} catch (IOException e) {
			return;
		}

		while (true) {
			try {
				KVAdminMessage inpMsg = KVAdminMessageManager.receiveKVAdminMessage(inp);
						
					switch (inpMsg.getCommand()) {
					case PING_FAILURE:
						Node nextNode = ECSServer.activeServers.getNextNode(inpMsg.getServer());
						FailureDetector.fixAndReplaceFailedNode(inpMsg.getServer());
						FailureDetector.startForwardPing(nextNode);
						break;	
					case PING_SUCCESS:
						logger.info(inpMsg.getServer().getName()+" is Alive - circle completed!");
						break;
					default:
						
						break;
					}
				

			} catch (Exception e) {

				return;
			}
		}
	}

}
