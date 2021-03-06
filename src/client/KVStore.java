package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import common.messages.KVMessage;
import common.messages.KVMessage.StatusType;
import common.messages.KVMessageManager;
import common.messages.impl.KVMessageImpl;

public class KVStore implements KVCommInterface {


	static Logger logger = Logger.getLogger(KVStore.class);

	String ip;
	int port;
	Socket sock;
	InputStream in;
	OutputStream out;
	KVMessageManager tool = new KVMessageManager();
    /**
     * Initialize KVStore with address and port of KVServer
     *
     * @param address the address of the KVServer
     * @param port    the port of the KVServer
     */
    public KVStore(String address, int port) {
    	this.ip = address;
    	this.port = port;
    	
    }

    @Override
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

    @Override
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

    
    @Override
    public KVMessage put(String key, String value) throws Exception {
    	if (sock == null || sock.isClosed()) {
			logger.warn("Attempting to connect via closed socket.");
			throw new IOException("No open connection to server.");
		} else {
			KVMessage msg;
			if(value.equals("null")){
				msg = new KVMessageImpl(key,StatusType.DELETE);
			} else {
				msg = new KVMessageImpl(key,value,StatusType.PUT);
			}
			logger.info("Attempting to update: " + key+" : "+value);
			KVMessageManager.sendKVMessage(msg,out);
			KVMessage recvdMsg = KVMessageManager.receiveKVMessage(in);
			logger.info("Server response: " + recvdMsg.toString());
			return recvdMsg;
		}
    }
    
    
    @Override
    public KVMessage transfer(String key, String value, LocalDateTime timestamp, boolean copyAndReplicate) throws Exception {
    	if (sock == null || sock.isClosed()) {
			logger.warn("Attempting to connect via closed socket.");
			throw new IOException("No open connection to server.");
		} else {
			KVMessage msg;
			msg = new KVMessageImpl(key,value, copyAndReplicate? StatusType.COPY_AND_REPLICATE :StatusType.COPY);
			msg.setTimestamp(timestamp);
			logger.info("Attempting to transfer: " + key+" : "+value);
			KVMessageManager.sendKVMessage(msg,out);
			KVMessage recvdMsg = KVMessageManager.receiveKVMessage(in);
			logger.info("Server response: " + recvdMsg.toString());
			System.out.println("Server response: " + recvdMsg.toString());
			return recvdMsg;
		}
    }

    @Override
    public KVMessage replicate(KVMessage msg) throws Exception {
    	if (sock == null || sock.isClosed()) {
			logger.warn("Attempting to connect via closed socket.");
			throw new IOException("No open connection to server.");
		} else {
			logger.info("Attempting to replicate: "+ msg);
			KVMessageManager.sendKVMessage(msg,out);
			KVMessage recvdMsg = KVMessageManager.receiveKVMessage(in);
			logger.info("Server response: " + recvdMsg.toString());
			System.out.println("Server response: " + recvdMsg.toString());
			return recvdMsg;
		}
    }
    
	@Override
    public KVMessage get(String key) throws Exception {
    	if (sock == null || sock.isClosed()) {
			logger.warn("Attempting to connect via closed socket.");
			throw new IOException("No open connection to server.");
		} else {

			KVMessage msg = new KVMessageImpl(key,StatusType.GET);
			
			logger.info("Attempting to retrieve: " + key);
			KVMessageManager.sendKVMessage(msg,out);
			KVMessage recvdMsg = KVMessageManager.receiveKVMessage(in);
			logger.info("Server response: " + recvdMsg.toString());
			return recvdMsg;

		}
    }

	/**
	 * @param level This method sets the level of logging according to priority
	 */
	public static void setLevel(String level) {
		Level prvLevel = logger.getLevel();

		logger.setLevel(Level.toLevel(level));
		logger.info("Changed log level from " + prvLevel + " to " + logger.getLevel());

	}

    
	

}
