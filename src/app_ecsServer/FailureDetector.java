package app_ecsServer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import app.common.HashRing;
import app.common.Node;
import common.messages.KVAdminMessage;
import common.messages.KVAdminMessage.Command;
import common.messages.impl.KVAdminMessageImpl;

public class FailureDetector {
	
	static Logger logger = Logger.getLogger(FailureDetector.class);
	
	public static void detectFailure() {
		int i=0;
		Node nodes[] = ECSServer.activeServers.getMetaData();
		logger.info( "Active servers " + HashRing.toString(ECSServer.activeServers.getMetaData()));
		startForwardPing(nodes[0],0,0);
		
		}
	
	public static void startForwardPing(Node node, int readStats, int writeStats) {
	
		try {
			String ipAddress = node.getIpAddress();
			int port = Integer.parseInt(node.getAdminPort());
			logger.info("Sending failure detection message to "+node.getName());
			ECSServerCommunicator client = new ECSServerCommunicator(ipAddress, port);
			client.connect();
			KVAdminMessage msg = new KVAdminMessageImpl();
			msg.setCommand(Command.PING_FORWARD);
			msg.setServer(node);
			msg.setReadStats(readStats);
			msg.setWriteStats(writeStats);
			KVAdminMessage recd = client.sendMessage(msg);
			logger.info("Status from KV Server: " + recd.getCommand());
			if(recd.getCommand()!=Command.PING_SUCCESS) {
				fixAndReplaceFailedNode(recd.getServer());
			}
			else {
				logger.info(node.getName()+" is alive!");
			}
			client.disconnect();
			
		} catch (TimeoutException e) {
			fixAndReplaceFailedNode(node);
		} catch (UnknownHostException e) {
			logger.info( "Unknown host. Unable to establish connection.");
			fixAndReplaceFailedNode(node);
		} catch (IOException e) {
			logger.info("FailureDetector: sendMessage: Unable to establish connection.");
			fixAndReplaceFailedNode(node);
		} catch (Exception e) {
			logger.info( e.getLocalizedMessage());
			fixAndReplaceFailedNode(node);

		}
	}
	
	public static void fixAndReplaceFailedNode(Node node) {
		int cacheSize = node.getCacheSize();
		String cacheStrategy = node.getCacheType();
		logger.info("FailureDetector: Detected node failure.\nAttempting to fix...");
		ECSServerLibrary.removeNode(ECSServer.serverConfig, ECSServer.activeServers, node);

		Node newNode = ECSServerLibrary.addNode(ECSServer.serverConfig, cacheSize, cacheStrategy, ECSServer.activeServers);
		
		if(ECSServer.serverState) {
		KVAdminMessage adminMsg = new KVAdminMessageImpl();
		adminMsg.setCommand(KVAdminMessage.Command.START);
		adminMsg.setMetaData(ECSServer.activeServers.getMetaData());
		adminMsg.setECSIP(ECSServer.ip);
		adminMsg.setPort(ECSServer.port);
		ECSServerLibrary.notifySingleServer(adminMsg, newNode);
		}
	
	}
	
	
}
