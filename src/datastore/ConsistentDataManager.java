package datastore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

import org.apache.log4j.Logger;

import app.common.Node;
import app_kvServer.KVServer;
import common.messages.KVAdminMessage;
import common.messages.KVAdminMessageManager;
import common.messages.KVMessage;
import common.messages.KVMessage.StatusType;
import common.messages.KVAdminMessage.Command;
import common.messages.impl.KVAdminMessageImpl;

public class ConsistentDataManager {

	static Logger logger = Logger.getLogger(ConsistentDataManager.class);
	
	public static String get(String key) throws Exception {
		
		
		Node[] nodes = KVServer.metaData.getNodesOfKey(key);		
		int i=0;
		
		for(;i<=KVServer.replicationFactor;i++) {
			if(nodes[i].getName().equals(KVServer.nodeName))
				break;
		}
		for(Node node: nodes) {
		logger.info(node.toString());
		}
		String value = DataManager.get(key);
		LocalDateTime latest = DataManager.getTimeStamp(key);
		i++;
		logger.info(KVServer.nodeName+"  "+Integer.toString(i));
		int j = KVServer.readConsistencyLevel-1;
		
		while(j>0) {
			KVAdminMessage msg = readReplica(nodes[i], key);
			
			if(msg.getCommand()==Command.GET_SUCCESS) {
			
			if(msg.getTimestamp().isAfter(latest)) {
				value = msg.getValue();
				latest = msg.getTimestamp();
				}
			}
			
			i = i<KVServer.replicationFactor?i+1:0;
			j--;
		}
			
		return value;
	}
	
	public static StatusType put(String key, String value) throws Exception {
		
		Node[] nodes = KVServer.metaData.getNodesOfKey(key);	
		
		int i=0;
		for(;i<=KVServer.replicationFactor;i++) {
			if(nodes[i].getName()==KVServer.nodeName)
				break;
		}
		i = i<KVServer.replicationFactor?i+1:0;
		LocalDateTime now=LocalDateTime.now();
		StatusType retType = DataManager.put(key, value, true, now);
		int j = KVServer.writeConsistencyLevel-1;
		int msgCount = 0;
		i++;
		
		while(j>0) {
			KVAdminMessage msg = writeToReplica(nodes[i], key, value, now);
			i = i<KVServer.replicationFactor?i+1:0;
			msgCount++;
			
			if(msg.getCommand()!=Command.PUT_SUCCESS && msgCount<=KVServer.replicationFactor)
				continue;
			j--;
		} 		
		
		
		return retType;
	}

	
	private static KVAdminMessage readReplica(Node toNode, String key) {
				
		KVAdminMessage outMsg = new KVAdminMessageImpl();	
		outMsg.setCommand(Command.GET);
		outMsg.setKey(key);
		outMsg.setServer(toNode);
		return send(outMsg,toNode);
	}
	
	private static KVAdminMessage writeToReplica(Node toNode, String key, String value, LocalDateTime timestamp) {
				
		KVAdminMessage outMsg = new KVAdminMessageImpl();		
		outMsg.setCommand(Command.PUT);
		outMsg.setServer(toNode);
		outMsg.setValue(value);
		outMsg.setTimestamp(timestamp);
		outMsg.setKey(key);
		return send(outMsg,toNode);
	}

	private static KVAdminMessage send(KVAdminMessage msg, Node toNode){
		
		KVAdminMessage inMsg;
		try {
			
			String ip = toNode.getIpAddress();
			int port = Integer.parseInt(toNode.getAdminPort());
			Socket sock;
			try {
				sock = new Socket(ip, port);
			} catch (UnknownHostException e) {
				return null;
				
			} catch (IOException e) {
				return null;
			}
			
			InputStream in = sock.getInputStream();
			OutputStream out = sock.getOutputStream();
			logger.info("Send consistency "+msg.getCommand()+" message to "+toNode.getName());
			logger.info("Sending msg "+msg.toString());
			
			KVAdminMessageManager.sendKVAdminMessage(msg, out);
			logger.info("Waiting for response message from "+toNode.getName());
			inMsg = KVAdminMessageManager.receiveKVAdminMessage(in);
			logger.info("Received msg "+inMsg.toString());
			sock.close();
			
			return inMsg; 
			
		} catch (IOException|ClassNotFoundException e) {
			return null;
		}
	}

}
