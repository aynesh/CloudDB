package app_kvServer;

import java.time.LocalDateTime;

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

	public void run() {
		logger.info("Started Replication Schedule: "+LocalDateTime.now());
		logger.info("Staring. Items in Queue "+ KVServer.queue.size());
		Node transferNode1 = KVServer.metaData.getNextNode(this.nodeName);
		Node transferNode2 = null;
		if(transferNode1 != null) {
			transferNode2 = KVServer.metaData.getNextNode(transferNode1);
		}
		
		
		for(KVMessage kvMessage: KVServer.queue) {
			
			if(transferNode1!=null) {
				try {
					if(this.transferData(transferNode1, kvMessage)) {
						if(this.transferData(transferNode2, kvMessage)) {
							KVServer.queue.remove(kvMessage);
							logger.info("replicated/updated Key: "+kvMessage.getKey());
						}
					}
				} catch(Exception ex) {
					logger.error(ex);
				}
				
				
				
			}
			
			
			//At The end remove from the queue
			
		}
		logger.info("Completed. Items Left in Queue "+ KVServer.queue.size());
	}
	
	
}
