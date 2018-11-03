package app_kvClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Arrays;

import client.KVStore;
import common.messages.KVMessage;;

public class KVClient {
	/**
	 * This method lists all the possible available actions that can be performed by
	 * a user
	 */
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

	/**
	 * @param args Unused.
	 * @return Nothing.
	 * @exception IOException On input error.
	 * @throws IOException
	 * this main method creates an instance of Client class,
	 * receives commands through BufferedReader as Strings
	 * and behaves accordingly. 
	 */
	public static void main(String[] args) throws IOException
	{

		BufferedReader cons = new BufferedReader(new InputStreamReader(System.in));
		boolean quit = false;

		KVStore client = null; 

		while (!quit)
		{
			System.out.print("Client> ");
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
						client = new KVStore(tokens[1], Integer.parseInt(tokens[2]));
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
			else if (tokens[0].equals("get"))
			{
				try{
					KVMessage recd = client.get(tokens[1]);
					System.out.println("Server> " + recd.getValue());
				} catch (UnsupportedEncodingException e) {
					System.out.println("Failed to decode message.");
					throw e;
				} catch (IOException e) {
					System.out.println("Failed to receive response.");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			else if (tokens[0].equals("put"))
			{
				try{
					KVMessage recd = client.put(tokens[1], tokens[2]);
					System.out.println("Server> " + recd.getStatus());
				} catch (UnsupportedEncodingException e) {
					System.out.println("Failed to decode message.");
					throw e;
				} catch (IOException e) {
					System.out.println("Failed to receive response.");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			else if (tokens[0].equals("disconnect"))
			{
					 client.disconnect();
				
			}
			else if (tokens[0].equals("help"))
			{
				help();
			}
			else if (tokens[0].equals("logLevel"))
			{
				if(tokens.length<2) {
					System.out.println("Incorrect usage of command.");
					help();
				}
				else {
					client.setLevel(tokens[1]);
					System.out.println("Changed log level.");
				}
			}
			else
			{
				invalidCommand();
			}
		}

		System.out.println("Client terminated.");

	}


}
