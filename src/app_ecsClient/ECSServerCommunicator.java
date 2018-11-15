package app_ecsClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import common.messages.KVAdminMessage;
import common.messages.KVAdminMessageManager;
import common.messages.KVMessage;
import common.messages.KVMessageManager;
import common.messages.KVMessage.StatusType;
import common.messages.impl.KVMessageImpl;

public class ECSServerCommunicator {
	static Logger logger = Logger.getLogger(ECSServerCommunicator.class);

	String ip;
	int port;
	Socket sock;
	InputStream in;
	OutputStream out;
	
    public ECSServerCommunicator(String address, int port) {
    	this.ip = address;
    	this.port = port;
    	
    }

    public void connect() throws Exception {
        // TODO Auto-generated method stub
    	try {
			// connect 131.159.52.2 50000
			logger.info("Attempting to establish connection to " + ip + " " + port);

			sock = new Socket(ip, port);
			in = sock.getInputStream();
			out = sock.getOutputStream();

		} catch (UnknownHostException e) {
			logger.error("Unknown host. Unable to establish connection.");
			throw e;
		} catch (IOException e) {
			logger.error("Unable to establish connection.");
			throw e;

		}
    }
    
    public void disconnect() {
    	if (sock == null || sock.isClosed()) {
			logger.error("No open connection.");
		}

		try {
			in.close();
			out.close();
			sock.close();
			logger.info("Diconnected from server.");
			
		} catch (IOException e) {
			logger.error("Failed to disconnect.");	
		}
    }
    
    public KVAdminMessage sendMessage(KVAdminMessage message) throws Exception {
    	if (sock == null || sock.isClosed()) {
			logger.warn("Attempting to connect via closed socket.");
			throw new IOException("No open connection to server.");
		} else {
			logger.info("Attempting to send : " + message);
			KVAdminMessageManager.sendKVAdminMessage(message, out);
			KVAdminMessage recvdMsg = KVAdminMessageManager.receiveKVAdminMessage(in);
			logger.info("Server response: " + recvdMsg.toString());
			return recvdMsg;
		}
    }


}
