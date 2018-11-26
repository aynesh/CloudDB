package app_ecsServer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import app.common.HashRing;
import app.common.Node;
import common.messages.KVAdminMessage;
import common.messages.KVAdminMessage.Command;
import common.messages.impl.KVAdminMessageImpl;

public class ECSServerLibrary {

	static Logger logger = Logger.getLogger(ECSServerLibrary.class);

	/**
	 * @param nodeIdentifier Node Name
	 * @param ipAddress Ip address of the node to be started.
	 * @param userName username of the system to connect.
	 * @param location The location of the jar file.
	 * @param port Port to start the system.
	 * @param adminPort Port for admin commands.
	 * @param cacheSize 
	 * @param cacheStrategy
	 * @param storagePath the storage path for key value pairs.
	 */
	public static void launchProcess(String nodeIdentifier, String ipAddress, String userName, String location,
			String port, String adminPort, int cacheSize, String cacheStrategy, String storagePath) {
		try {
			JSch jsch = new JSch();

			String host = userName + "@" + ipAddress;
			String user = host.substring(0, host.indexOf('@'));
			String privateKey = "/home/" + userName + "/.ssh/id_rsa";
			host = host.substring(host.indexOf('@') + 1);

			Session session = jsch.getSession(user, host, 22);
			jsch.addIdentity(privateKey);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			String command;

			command = "java -jar " + location + " " + nodeIdentifier + " " + port + " " + adminPort + " " + cacheSize
					+ " " + cacheStrategy + " " + storagePath;

			logger.info( command);

			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			
			InputStream in = channel.getInputStream();
		    ((ChannelExec)channel).setErrStream(System.err);
			channel.connect();

			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					logger.info( new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;
					logger.info("exit-status: " + channel.getExitStatus());
					break;
				}
			}

			// channel.sendSignal("2"); // CTRL + C - interrupt
			channel.sendSignal("2 "); // KILL
			channel.disconnect();
			session.disconnect();
		} catch (Exception e) {
			logger.info(e.getLocalizedMessage());
		}
	}

	/**
	 * @param fileName
	 * @return
	 */
	public static Map<String, Node> readConfigFile(String fileName) {
		Map<String, Node> serverConfig = new HashMap();

		FileReader fileReader;
		try {
			fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String readLine = bufferedReader.readLine();
			while (readLine != null) {
				String values[] = readLine.split(" ");
				Node node = new Node(values[0], values[1], values[2], values[3], values[4], values[5], values[6]);
				serverConfig.put(values[0], node);
				readLine = bufferedReader.readLine();
			}

			bufferedReader.close();

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return serverConfig;
	}

	public static KVAdminMessage sendMessage(KVAdminMessage msg, String ipAddress, int port) {
		try {
			logger.info( "Sending admin message: " + ipAddress + ":" + port);
			ECSServerCommunicator client = null;
			client = new ECSServerCommunicator(ipAddress, port);
			client.connect();
			KVAdminMessage recd = client.sendMessage(msg);
			logger.info("Status from KV Server: " + recd.getCommand());
			client.disconnect();
			return recd;
		} catch (UnknownHostException e) {
			logger.info( "Unknown host. Unable to establish connection.");
		} catch (IOException e) {
			logger.info("ECSServerLibrary: sendMessage: Unable to establish connection.");
		} catch (Exception e) {
			logger.info( e.getLocalizedMessage());

		}
		return null;
	}
	

	/**
	 * @param serverConfig list of configured servers.
	 * @param cacheSize
	 * @param cacheStrategy
	 * @param numberOfServers number of servers to launch.
	 * @param activeServers
	 */
	public static void launchServers(Map<String, Node> serverConfig, int cacheSize, String cacheStrategy,
			int numberOfServers, HashRing activeServers) {
		int i = 1;
		ArrayList<String> itemsToRemove = new ArrayList();
		for (Map.Entry<String, Node> item : serverConfig.entrySet()) {
			if (i > numberOfServers)
				break;

			new Thread(new Runnable() {
				@Override
				public void run() {
					// Please configure SSH keys for ur system and set the path in ECSServerLibaray
					// for now
					Node node = item.getValue();
					ECSServerLibrary.launchProcess(node.getName(), node.getIpAddress(), node.getUserName(),
							node.getLocation(), node.getPort(), node.getAdminPort(), cacheSize, cacheStrategy,
							node.getStoragePath());

					// Else comment the above code and uncomment the below code.
					// new KVServer(50000, 10, "LFU")
				}
			}).start();

			activeServers.addNode(item.getValue());
			itemsToRemove.add(item.getKey());
			i++;
		}
		for (String key : itemsToRemove) {
			serverConfig.remove(key);
		}

	}

	/**
	 * @param serverConfig
	 * @param cacheSize
	 * @param cacheStrategy
	 * @param activeServers
	 */
	public static void addNode(Map<String, Node> serverConfig, int cacheSize, String cacheStrategy,  HashRing activeServers) {
		int i = 1;
		String keyToRemove = null;
		Node newNode = null;
		for (Map.Entry<String, Node> item : serverConfig.entrySet()) {
			if (i > 1)
				break;

			new Thread(new Runnable() {
				@Override
				public void run() {
					Node node = item.getValue();
					ECSServerLibrary.launchProcess(node.getName(), node.getIpAddress(), node.getUserName(),
							node.getLocation(), node.getPort(), node.getAdminPort(), cacheSize, cacheStrategy,
							node.getStoragePath());
				}
			}).start();

			newNode = item.getValue();
			i++;

		}

		activeServers.addNode(newNode);
		keyToRemove = newNode.getName();

		Node prevNode = activeServers.getPrevNode(newNode);
		Node nextNode = activeServers.getNextNode(newNode);

		writeLockUnlockServers(newNode, prevNode, true);
		writeLockUnlockServers(newNode, nextNode, true);

		boolean serverOnline = false;
		KVAdminMessageImpl pingMessage = new KVAdminMessageImpl();
		pingMessage.setCommand(Command.PING);
		i=1;
		while (!serverOnline) {
			if(i > 100) {
				break;
			}
			KVAdminMessage reply = ECSServerLibrary.sendMessage(pingMessage, newNode.getIpAddress(),
					Integer.parseInt(newNode.getAdminPort()));
			if (reply != null) {
				if (reply.getCommand() == Command.PING_SUCCESS) {
					serverOnline = true;
					logger.info("Ping success: "+newNode.getIpAddress());
				}
			}
			try {
				logger.info("Ping sleep... attempting next ping "+i);
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.error(e);
			}
			i++;
		}

		initiateTransferFiles(newNode, prevNode, nextNode, activeServers.getMetaData());

		updateMetaData(newNode, activeServers);
		updateMetaData(prevNode, activeServers);
		updateMetaData(nextNode, activeServers);

		writeLockUnlockServers(newNode, prevNode, false);
		writeLockUnlockServers(newNode, nextNode, false);

		// Transfer Keys

		serverConfig.remove(keyToRemove);

	}

	/**
	 * Update meta Data of a target Server.
	 * @param targetNode
	 * @param activeServers
	 */
	public static void updateMetaData(Node targetNode, HashRing activeServers) {
		KVAdminMessageImpl msg = new KVAdminMessageImpl();
		msg.setCommand(Command.META_DATA_UPDATE);
		msg.setMetaData(activeServers.getMetaData());
		notifySingleServer(msg, targetNode);
	}

	/**
	 * Remove Node.
	 * @param serverConfig
	 * @param activeServers
	 */
	public static void removeNode(Map<String, Node> serverConfig,  HashRing activeServers) {
		String key = null;
		Node selectedNode = null;
		Node nodes[] = activeServers.getMetaData();

		if (nodes.length == 0) {
			return;
		}
		selectedNode = nodes[0];

		Node nextNode = activeServers.getNextNode(selectedNode);
		Node prevNode = activeServers.getPrevNode(selectedNode);

		activeServers.removeNode(selectedNode);

		writeLockUnlockServers(selectedNode, selectedNode, true);
		writeLockUnlockServers(selectedNode, nextNode, true);

		initiateTransferFilesForRemove(selectedNode, prevNode, nextNode, activeServers.getMetaData());

		updateMetaData(nextNode, activeServers);

		// writeLockUnlockServers(item.getValue(), item.getValue(), false); // Server
		// Shutdown Not needed
		writeLockUnlockServers(selectedNode, nextNode, false);

		// Transfer Keys

		// Shutdwon is implmeneted at the KV Server itself
		serverConfig.put(selectedNode.getName(), selectedNode);
	}

	/**
	 * @param referenceNode
	 * @param targetNode
	 * @param lock
	 */
	public static void writeLockUnlockServers(Node referenceNode, Node targetNode, boolean lock) {
		KVAdminMessageImpl msg = new KVAdminMessageImpl();
		msg.setCommand(lock ? KVAdminMessage.Command.SERVER_WRITE_LOCK : KVAdminMessage.Command.SERVER_WRITE_UNLOCK);
		if (!targetNode.getName().equals(referenceNode.getName())) {
			notifySingleServer(msg, targetNode);
		}
	}

	// Repeated can be optimized
	/**
	 * @param currentNode
	 * @param prevNode
	 * @param nextNode
	 * @param metaData
	 */
	public static void initiateTransferFilesForRemove(Node currentNode, Node prevNode, Node nextNode, Node[] metaData) {
		KVAdminMessageImpl msg = new KVAdminMessageImpl();
		msg.setCommand(Command.TRANSFER_AND_SHUTDOWN); // Below is a blocking operation !
		msg.setMetaData(metaData);
		if (!nextNode.getName().equals(currentNode.getName())) {
			msg.setTransferStartKey(prevNode.getEndRange());
			msg.setTransferEndKey(currentNode.getEndRange());
			msg.setTransferServer(nextNode);
			notifySingleServer(msg, currentNode);
		}
	}

	/**
	 * @param newNode
	 * @param prevNode
	 * @param nextNode
	 * @param metaData
	 */
	public static void initiateTransferFiles(Node newNode, Node prevNode, Node nextNode, Node[] metaData) {
		KVAdminMessageImpl msg = new KVAdminMessageImpl();
		msg.setCommand(Command.TRANSFER); // Below is a blocking operation !
		msg.setMetaData(metaData);
		if (!prevNode.getName().equals(newNode.getName())) {
			msg.setTransferServer(newNode);
			notifySingleServer(msg, prevNode);
		}
		if (!nextNode.getName().equals(newNode.getName())) {
			msg.setTransferServer(newNode);
			notifySingleServer(msg, nextNode);
		}
	}

	/**
	 * @param msg
	 * @param activeServers
	 */
	public static void notifyAllServers(KVAdminMessage msg, HashRing activeServers) {
		for (Node node : activeServers.getMetaData()) {
			ECSServerLibrary.sendMessage(msg, node.getIpAddress(), Integer.parseInt(node.getAdminPort()));
		}
	}

	/**
	 * @param msg
	 * @param node
	 */
	public static void notifySingleServer(KVAdminMessage msg, Node node) {
		ECSServerLibrary.sendMessage(msg, node.getIpAddress(), Integer.parseInt(node.getAdminPort()));
	}

}
