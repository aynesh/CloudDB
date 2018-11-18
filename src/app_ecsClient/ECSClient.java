package app_ecsClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import common.messages.KVAdminMessage;
import common.messages.KVAdminMessage.Command;
import common.messages.impl.KVAdminMessageImpl;

public class ECSClient {
	
	
	/**
	 * This method lists all the possible available actions that can be performed by
	 * a user
	 */
	static Logger logger = Logger.getLogger(ECSClient.class);
	public static void help() {
		System.out.println("Intended usage of available commands:");
		System.out.println(
				"connect <address> <port> - Tries to establish a TCP- connection to the ECS server at <address> and <port>.");
		System.out.println("disconnect - Tries to disconnect from ECS server");
		System.out.println("initService <number of nodes> <Cache Size> <Cache Strategy> - Initialize n number of servers");
		System.out.println("start - Start Receiving client calls");
		System.out.println("stop - Stop receiving client calls");
		System.out.println("shutdown - Shutdown all servers");
		System.out.println("addNode cacheSize cacheType - Add a new server at arbitrary position");
		System.out.println("removeNode - Remove a Server");
	}

	/**
	 * This method notifies user in case the latter calls unknown command
	 */
	public static void invalidCommand() {
		System.out.println("Unknown command.");
		help();
	}

	/**
	 * @param args Unused.
	 * @return Nothing.
	 * @exception IOException On input error.
	 * @throws IOException
	 * this main method creates an instance of Client class,
	 * receives commands through BufferedReader as Strings
	 * and behaves accordingly. 
	 */
	public static void main(String args[]) throws IOException {
		BufferedReader cons = new BufferedReader(new InputStreamReader(System.in));
		boolean quit = false;

		ECSServerCommunicator client = null; 

		while (!quit)
		{
			System.out.print("ECSClient> ");
			String input = cons.readLine();
			String[] tokens = input.trim().split("\\s+");
			if (tokens[0].equals("quit"))
			{
				quit = true;
				
			}
			else if (tokens[0].equals("connect"))
			{
				if(tokens.length<3) {
					System.out.println("Incorrect usage of command.");
					help();
				}
				else {
					try {
						client = new ECSServerCommunicator(tokens[1], Integer.parseInt(tokens[2]));
						client.connect();
						
					} catch (UnknownHostException e) {
						System.out.println("Unknown host. Unable to establish connection.");
					} catch (IOException e) {
						System.out.println("Unable to establish connection.");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				}
			}
			else if (tokens[0].equals("initService"))
			{
				if(tokens.length<4) {
					System.out.println("Incorrect usage of command.");
					help();
				}
				
				
				try{
					KVAdminMessageImpl msg = new KVAdminMessageImpl();
					msg.setCommand(KVAdminMessage.Command.INIT_SERVICE);
					msg.setNumberOfNodes(Integer.parseInt(tokens[1]));
					msg.setCacheSize(Integer.parseInt(tokens[2]));
					msg.setCacheType(tokens[3]);
					KVAdminMessage recd= client.sendMessage(msg);
					if(recd.getCommand()==Command.INIT_SERVICE_SUCCESS) {
						System.out.println("Server> Initialized");
					}
					else {
						System.out.println("Server> Error");
					}
						
					
				} catch (UnsupportedEncodingException e) {
					System.out.println("Failed to decode message.");
					throw e;
				} catch (IOException e) {
					System.out.println("Failed to receive response.");
				} catch (NullPointerException e) {
					System.out.println("Connection not established.");
				}
				catch (Exception e) {
					System.out.println("Error.");
				}


			} 
			else if (tokens[0].equals("start"))
			{
				
				
				try{
					KVAdminMessageImpl msg = new KVAdminMessageImpl();
					msg.setCommand(KVAdminMessage.Command.START);
					KVAdminMessage recd= client.sendMessage(msg);
					if(recd.getCommand()==Command.START_SUCCESS) {
						System.out.println("Server> Start Sent");
					}
					else {
						System.out.println("Server> Error");
					}
						
					
				} catch (UnsupportedEncodingException e) {
					System.out.println("Failed to decode message.");
					throw e;
				} catch (IOException e) {
					System.out.println("Failed to receive response.");
				} catch (NullPointerException e) {
					System.out.println("Connection not established.");
				}
				catch (Exception e) {
					System.out.println("Error.");
				}


			}
			else if (tokens[0].equals("stop"))
			{
				
				
				try{
					KVAdminMessageImpl msg = new KVAdminMessageImpl();
					msg.setCommand(KVAdminMessage.Command.STOP);
					KVAdminMessage recd= client.sendMessage(msg);
					if(recd.getCommand()==Command.STOP_SUCCESS) {
						System.out.println("Server> Stop Sent");
					}
					else {
						System.out.println("Server> Error");
					}
						
					
				} catch (UnsupportedEncodingException e) {
					System.out.println("Failed to decode message.");
					throw e;
				} catch (IOException e) {
					System.out.println("Failed to receive response.");
				} catch (NullPointerException e) {
					System.out.println("Connection not established.");
				}
				catch (Exception e) {
					System.out.println("Error.");
				}


			}
			
			else if (tokens[0].equals("shutdown"))
			{
				
				
				try{
					KVAdminMessageImpl msg = new KVAdminMessageImpl();
					msg.setCommand(KVAdminMessage.Command.SHUTDOWN);
					KVAdminMessage recd= client.sendMessage(msg);
					if(recd.getCommand()==Command.SHUTDOWN_SUCCESS) {
						System.out.println("Server> Shutdown initiated");
					}
					else {
						System.out.println("Server> Error");
					}
						
					
				} catch (UnsupportedEncodingException e) {
					System.out.println("Failed to decode message.");
					throw e;
				} catch (IOException e) {
					System.out.println("Failed to receive response.");
				} catch (NullPointerException e) {
					System.out.println("Connection not established.");
				}
				catch (Exception e) {
					System.out.println("Error.");
				}


			}
			else if (tokens[0].equals("disconnect"))
			{
					 try{
						 client.disconnect();
					 }
					 
						 catch (NullPointerException e) {
								System.out.println("Connection not established.");
							}
					 
				
			}
			else if (tokens[0].equals("help"))
			{
				help();
			}
			else
			{
				invalidCommand();
			}
		}

		System.out.println("ECSClient terminated.");
	}
}
