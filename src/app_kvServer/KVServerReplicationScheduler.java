package app_kvServer;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import app.common.Node;
import client.KVStore;
import common.messages.KVMessage;
import common.messages.KVMessage.StatusType;

public class KVServerReplicationScheduler extends Thread {
	
	static Logger logger = Logger.getLogger(KVServerReplicationScheduler.class);
	
	private String nodeName;
	
	public KVServerReplicationScheduler(String nodeName) {
		this.nodeName = nodeName;
	}
	
	public boolean transferData(Node toNode, KVMessage msg) throws Exception {
		KVStore kvClient = new KVStore(toNode.getIpAddress(), Integer.parseInt(toNode.getPort()));
		kvClient.connect();
		KVMessage response = kvClient.replicate(msg);
		logger.debug("Replication Response : "+response.getStatus());
		kvClient.disconnect();
		if(response.getStatus()==StatusType.COPY_SUCCESS || response.getStatus()==StatusType.DELETE_REPLICA_COPY_SUCCESS) {
			logger.info("Updated: "+response);
			return true;
		}
		
		return false;

	}

	public synchronized void run() {
		logger.info("Started Replication Schedule: "+LocalDateTime.now());
		logger.info("Staring. Items in Queue "+ KVServer.queue.size());
		
		ArrayList<KVMessage> keysToRemove = new ArrayList<KVMessage>();

		for (KVMessage kvMessage : KVServer.queue) {
			Node[] nextNodes;
			try {
				nextNodes = KVServer.metaData.getNodesOfKey(kvMessage.getKey());
			} catch (NoSuchAlgorithmException e) {
				logger.info("Cannot continue replication nexNodes not found");;
				break;
			}
			for (int i = 0; i < nextNodes.length; i++) {
				try {
					if (!nextNodes[i].getName().equals(nodeName)) {
						this.transferData(nextNodes[i], kvMessage);
					}
				} catch (Exception ex) {
					logger.error(ex);
					logger.error("Incomplete Replication for key: " + kvMessage.getKey());

				}
			}
			keysToRemove.add(kvMessage);

			// At The end remove from the queue

		}
		
		for(KVMessage kvMessage: keysToRemove) {
			KVServer.queue.remove(kvMessage);
			logger.info("Removed key from queue: "+kvMessage.getKey());
		}
		
		
		logger.info("Completed. Items Left in Queue "+ KVServer.queue.size());
	}
	
	
}
