package app_kvClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLEngineResult.Status;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import app.common.HashRing;
import app.common.Node;
import client.KVStore;
import common.messages.KVMessage;
import common.messages.KVMessage.StatusType;;

public class KVClient {
	/**
	 * This method lists all the possible available actions that can be performed by
	 * a user
	 */
	static Logger logger = Logger.getLogger(KVClient.class);
	
	private KVStore client = null;
	
	private Node[] metaData=null;

	public static void help() {
		System.out.println("Intended usage of available commands:");
		System.out.println(
				"connect <address> <port> - Tries to establish a TCP- connection to the server at <address> and <port>.");
		System.out.println("disconnect - Tries to disconnect from server");
		System.out.println("send <message> - Tries to send <message> to connected server");
		System.out.println("logLevel <level> - Tries to set logger level to <level>");
		System.out.println("quit - quits the echo client");
		System.out.println("help - displays list of available commands");
	}

	/**
	 * This method notifies user in case the latter calls unknown command
	 */
	public static void invalidCommand() {
		System.out.println("Unknown command.");
		help();
	}

	public static void printError(KVMessage msg) {
		if (msg.getStatus() == StatusType.SERVER_STOPPED) {
			System.out.println("Server> Server Stopped");
		} else {
			System.out.println("Server> Error. "+msg.getStatus());
			System.out.println("Error> "+msg.getValue());
		}
		
	}
	
	public static void printGetOutut(KVMessage recd) {
		if (recd.getStatus() == StatusType.GET_SUCCESS) {
			System.out.println("Server> " + recd.getValue());
		} else {
			KVClient.printError(recd);
		}
	}
	
	
	public static void printPutOutput(KVMessage recd) {
		if(recd.getStatus()==StatusType.PUT_SUCCESS || recd.getStatus() == StatusType.PUT_UPDATE || recd.getStatus() == StatusType.DELETE_SUCCESS) {
			System.out.println("Server> " + recd.getStatus());
		} else {
			KVClient.printError(recd);
		}
	}
	
	public void Connect(String tokens[]) throws Exception {
		client = new KVStore(tokens[1], Integer.parseInt(tokens[2]));
		client.connect();
	}
	
	public void connectToAnyOtherServer() throws UnknownHostException {
		System.out.println("Client> Current Server Down. Connecting to any availbe servers.");
		if(metaData==null) {
			System.out.println("Client> No Meta Data Available.");
			throw new UnknownHostException("No Meta Data Available");
		}
		client.disconnect();
		KVMessage recd = null;
		for(int i=0; i<metaData.length; i++) {
			
			try {
				client=new KVStore(metaData[i].getIpAddress(), Integer.parseInt(metaData[i].getPort()));
				client.connect();
				return; //If connection successful 
			} catch (Exception e) {
				logger.error("Connect to "+metaData[i].getIpAndPort()+" failed.");
			}
		}
		throw new UnknownHostException("Cannot Connect to Any Servers :(");
	}
	
	public void Get(String key) throws Exception {
		KVMessage recd = null;
		try {
			recd = client.get(key);
			metaData=recd.getMetaData();
		} catch(IOException ex) {
			connectToAnyOtherServer();
			recd = client.get(key);
		}

		if(recd.getStatus() == StatusType.SERVER_NOT_RESPONSIBLE) {
			System.out.println("Server> SERVER_NOT_RESPONSIBLE");
			HashRing.printMetaData(recd.getMetaData());
			metaData = recd.getMetaData();
			client.disconnect();

			System.out.println("Client> Disconnected For Retrying. . .");
			
			client=retryServerNotReponsible(client, StatusType.GET, key, null,recd.getMetaData());
		} else {
			printGetOutut(recd);
		}
	}
	
	public void Put(String key, String value) throws Exception {
		KVMessage recd;
		try {
			recd = client.put(key, value != null ? value: "null");
			metaData=recd.getMetaData();
		} catch(IOException ex) {
			connectToAnyOtherServer();
			recd = client.put(key, value != null ? value: "null");
		}
		
		if(recd.getStatus() == StatusType.SERVER_NOT_RESPONSIBLE) {
			System.out.println("Server> SERVER_NOT_RESPONSIBLE");
			HashRing.printMetaData(recd.getMetaData());
			metaData = recd.getMetaData();
			client.disconnect();
			
			System.out.println("Client> Disconnected For Retrying. . .");
			
			client=retryServerNotReponsible(client, StatusType.PUT, key, value != null ? value: "null" ,recd.getMetaData());
			
		} else {
			KVClient.printPutOutput(recd);
		}
		
	}
	
	public void Disconnect() {
		client.disconnect();
	}
	

	
	public static KVStore retryServerNotReponsible(KVStore kvStore, StatusType request, String key, String value, Node[] metaData) {
		HashRing hashRing=new HashRing();
		hashRing.clearAndSetMetaData(metaData);
		Node responsibleNode=null;
		try {
			responsibleNode=hashRing.getNode(key);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block|
			e.printStackTrace();
		} 
		if(responsibleNode==null) {
			System.out.println("Client> Node not found!");
			System.out.println("Client> Please connect again !");
			return null;
		}
		
		kvStore = new KVStore(responsibleNode.getIpAddress(), Integer.parseInt(responsibleNode.getPort()));
		try {
			kvStore.connect();
			System.out.println("Client> Connected to "+responsibleNode.getIpAddress()+":"+responsibleNode.getPort());
		} catch (Exception e) {
			System.out.println("Client> "+e.getLocalizedMessage());
			System.out.println("Client> Please connect again !");
			return null;
		}
		
		if(request == StatusType.GET) {
			KVMessage recd;
			try {
				recd = kvStore.get(key);
				metaData = recd.getMetaData();
				printGetOutut(recd);
			} catch (Exception e) {
				System.out.print("Error> "+e.getMessage());
				kvStore.disconnect();
			}

		} else { //Its a put
			KVMessage recd;
			try {
				recd = kvStore.put(key, value);
				metaData = recd.getMetaData();
				printPutOutput(recd);
			} catch (Exception e) {
				System.out.print("Error> "+e.getMessage());
				kvStore.disconnect();
			}
		}
		return kvStore;
	}

	/**
	 * @param args Unused.
	 * @return Nothing.
	 * @exception IOException On input error.
	 * @throws IOException this main method creates an instance of Client class,
	 *                     receives commands through BufferedReader as Strings and
	 *                     behaves accordingly.
	 */
	public static void main(String[] args) throws IOException {
		
		Node[] metaData;
		HashRing hashRing= new HashRing();
		BufferedReader cons = new BufferedReader(new InputStreamReader(System.in));
		boolean quit = false;
		
		KVClient kvClient = new KVClient();


		while (!quit) {
			System.out.print("Client> ");
			String input = cons.readLine();
			String[] tokens = input.trim().split("\\s+");
			if (tokens[0].equals("quit")) {
				quit = true;

			} else if (tokens[0].equals("connect")) {
				if (tokens.length < 3) {
					System.out.println("Incorrect usage of command.");
					help();
				} else {
					try {
						kvClient.Connect(tokens);

					} catch (UnknownHostException e) {
						System.out.println("Unknown host. Unable to establish connection.");
					} catch (IOException e) {
						System.out.println("Unable to establish connection.");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			} else if (tokens[0].equals("get")) {
				if (tokens.length < 2) {
					System.out.println("Incorrect usage of command.");
					help();
				}

				try {
					kvClient.Get(tokens[1]);

				} catch (UnsupportedEncodingException e) {
					System.out.println("Failed to decode message.");
					throw e;
				} catch (IOException e) {
					System.out.println("Failed to receive response.");
				} catch (NullPointerException e) {
					System.out.println("Connection not established.");
				} catch (Exception e) {
					System.out.println("GET Failed. Reconnect again.");
				}

			} else if (tokens[0].equals("put")) {
				if (tokens.length < 2) {
					System.out.println("Incorrect usage of command.");
					help();
				}
				
				try {
					kvClient.Put(tokens[1], tokens.length > 2 ? tokens[2]: null );
				} catch (UnsupportedEncodingException e) {
					System.out.println("Failed to decode message.");
					throw e;
				} catch (IOException e) {
					System.out.println("Failed to receive response.");
				} catch (NullPointerException e) {
					System.out.println("Connection not established.");
				} catch (Exception e) {
					System.out.println("PUT Failed.Reconnect again");
				}

			} else if (tokens[0].equals("disconnect")) {
				try {
					kvClient.Disconnect();
				}
				catch (NullPointerException e) {
					System.out.println("Connection not established.");
				}

			} else if (tokens[0].equals("help")) {
				help();
			} else if (tokens[0].equals("logLevel")) {
				if (tokens.length < 2) {
					System.out.println("Incorrect usage of command.");
					help();
				} else {
					KVStore.setLevel(tokens[1]);
					System.out.println("Changed log level.");
				}
			} else {
				invalidCommand();
			}
		}

		System.out.println("Client terminated.");

	}

}
