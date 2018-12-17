package app_ecsServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import app.common.HashRing;
import app.common.Node;
import app_kvServer.KVServerReplicationScheduler;
import common.messages.KVAdminMessage;
import common.messages.impl.KVAdminMessageImpl;

public class ECSServer {

	static Logger logger = Logger.getLogger(ECSServer.class);
	
	public static HashRing activeServers;
	
	public static Map<String, Node> serverConfig;
	
	public static int port;

	public static String ip;
	public static boolean  serverState = false;
	private final ScheduledExecutorService scheduler;
	private final ExecutorService executor;
	
	public ECSServer() {

		
		scheduler = Executors.newScheduledThreadPool(1);
	    executor = Executors.newFixedThreadPool(1);
	   
	}
	
	public static void help() {
		System.out.println("Intended usage of available commands:");
		System.out.println(
				"initService <number of nodes> <Cache Size> <Cache Strategy> - Initialize n number of servers");
		System.out.println("start - Start Receiving client calls");
		System.out.println("stop - Stop receiving client calls");
		System.out.println("shutdown - Shutdown all servers");
		System.out.println("addNode cacheSize cacheType - Add a new server at arbitrary position");
		System.out.println("removeNode - Remove a Server");
		System.out.println("metaData -  meta Data of Servers");
		System.out.println("quit - shutdown ECS Server");
	}

	/**
	 * This method notifies user in case the latter calls unknown command
	 */
	public static void invalidCommand() {
		System.out.println("Unknown command.");
		help();
	}
	
	public void initService(String tokens[]) {
		try {
			if (Integer.parseInt(tokens[1]) > serverConfig.size()) {
				System.out.println("Server> Error --- Not that many Servers available");
			} else {
				ECSServerLibrary.launchServers(serverConfig, Integer.parseInt(tokens[2]), tokens[3],
						Integer.parseInt(tokens[1]), activeServers);
				System.out.println("Server> Initialized");
				HashRing.printMetaData(activeServers.getMetaData());
			}

		} catch (Exception e) {
			System.out.println("Unknown Error: " + e.getMessage());
		}
	}

	public void start() {
		KVAdminMessageImpl adminMsg = new KVAdminMessageImpl();
		adminMsg.setCommand(KVAdminMessage.Command.START);
		adminMsg.setMetaData(activeServers.getMetaData());
		adminMsg.setECSIP(ip);
		adminMsg.setPort(port);
		ECSServerLibrary.notifyAllServers(adminMsg, activeServers);
		
		executor.submit(new Runnable() {
		       public void run() { ECSReceiver receiver = new ECSReceiver(ECSServer.port); }
		     });
		scheduler.scheduleWithFixedDelay(new Runnable() {
		       public void run() { FailureDetector.detectFailure(); }
		     }, 20, 20, TimeUnit.SECONDS);
		     
	}
	
	public void stop() {
		KVAdminMessageImpl adminMsg = new KVAdminMessageImpl();
		adminMsg.setCommand(KVAdminMessage.Command.STOP);
		adminMsg.setMetaData(activeServers.getMetaData());
		ECSServerLibrary.notifyAllServers(adminMsg, activeServers);
		
	}
	
	public void shutdown(String fileName) {
		KVAdminMessageImpl adminMsg = new KVAdminMessageImpl();
		adminMsg.setCommand(KVAdminMessage.Command.SHUTDOWN);
		ECSServerLibrary.notifyAllServers(adminMsg, activeServers);
		activeServers.removeAll();
		serverConfig = ECSServerLibrary.readConfigFile(fileName);
		scheduler.shutdownNow();
		executor.shutdownNow();
	}
	
	public void addNode(String tokens[]) {
		ECSServerLibrary.addNode(serverConfig, Integer.parseInt(tokens[1]), tokens[2], activeServers);
		System.out.println("Server> Add Node Success.");
		HashRing.printMetaData(activeServers.getMetaData());
	}
	
	public void removeNode() {
		ECSServerLibrary.removeNode(serverConfig, activeServers);
		System.out.println("Server> Remove Node Success.");

		HashRing.printMetaData(activeServers.getMetaData());
	}
	
	public void printMetaData() {
		HashRing.printMetaData(activeServers.getMetaData());
	}

	public static void main(String[] args) {
		String ecsConfigFileName= (args.length > 0) ? args[0]: "ecs.config";
		
		ECSServer ecsServer = new ECSServer();
		ECSServer.port = args.length == 0? 40000: Integer.parseInt(args[0]);
		
		try {
			ecsServer.ip = InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e1) {
			ecsServer.ip = "localhost";
		}
		ecsServer.ip = "localhost";
		ecsServer.initializeActiveServers();
		ecsServer.initializeServerConfig(ecsConfigFileName);
		
		boolean quit = false;
		Scanner cons = new Scanner(System.in);
		
		
		while (true) {
			System.out.print("ECSServer> ");
			String input=cons.nextLine();

			String[] tokens = input.trim().split("\\s+");
			if (tokens[0].equals("quit")) {
				quit = true;

			} else if (tokens[0].equals("initService")) {
				if (tokens.length < 4) {
					System.out.println("Incorrect usage of command.");
					help();
				} else {
					ecsServer.initService(tokens);
				}
				
				
			} else if (tokens[0].equals("start")) {

				try {
					serverState = true; 
					ecsServer.start();
					System.out.println("Server> Start Sent");
				} catch (Exception e) {
					System.out.println("Error : " + e.getMessage());
				}

			} else if (tokens[0].equals("stop")) {

				try {
					serverState = false;
					ecsServer.stop();
					System.out.println("Server> Stop Sent");
				} catch (Exception e) {
					System.out.println("Error.");
				}

			}

			else if (tokens[0].equals("shutdown")) {

				try {
					
					ecsServer.shutdown(ecsConfigFileName);
					System.out.println("Server> Shutdown initiated");

				} catch (Exception e) {
					System.out.println("Error.");
				}

			} else if (tokens[0].equals("addNode")) {
				if (tokens.length < 3) {
					System.out.println("Incorrect usage of command.");
					help();
				} else {
					try {
						ecsServer.addNode(tokens);
					} catch (Exception e) {
						System.out.println("Error.");
					}
				}

			} else if (tokens[0].equals("removeNode")) {

				try {
					ecsServer.removeNode();
				}

				catch (Exception e) {
					System.out.println("Error.");
				}

			} else if (tokens[0].equals("metaData")) {

				ecsServer.printMetaData();

			} else if (tokens[0].equals("help")) {
				help();
			} else {
				invalidCommand();
			}
			
			if(quit) {
				System.out.println("Exiting CLI");
				System.exit(0);
				break;
				
			}
		}
	}

	public HashRing getActiveServers() {
		return activeServers;
	}

	public void initializeActiveServers() {
		this.activeServers =  new HashRing();
	}

	public Map<String, Node> getServerConfig() {
		return serverConfig;
	}

	public void initializeServerConfig(String fileName) {
		this.serverConfig = ECSServerLibrary.readConfigFile(fileName);
	}

}
