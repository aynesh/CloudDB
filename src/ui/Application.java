package ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.log4j.Logger;

import client.Client;

import org.apache.log4j.BasicConfigurator;

public class Application
{
	

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

		Client client = new Client();

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
						String recd = client.connect(tokens[1], tokens[2]);
						System.out.println("Server> " + recd);
					} catch (UnknownHostException e) {
						System.out.println("Unknown host. Unable to establish connection.");
					} catch (IOException e) {
						System.out.println("Unable to establish connection.");
					}
				
				}
			}
			else if (tokens[0].equals("send"))
			{
				try{
					String recd = new String(client.sendMessage(String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length))), "UTF-8").trim();
					System.out.println("Server> " + recd);
				} catch (UnsupportedEncodingException e) {
					System.out.println("Failed to decode message.");
					throw e;
				} catch (IOException e) {
					System.out.println("Failed to receive response.");
				}

			}
			else if (tokens[0].equals("disconnect"))
			{
				try {
					boolean disconnect = client.disconnect();
					if(disconnect) {
						System.out.println("Diconnected from server.");
					} else {
						System.out.println("No open connection.");
					}
				} catch (IOException e) {
					System.out.println("Diconnect failed.");
				}
				
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

		client.terminate();
		System.out.println("Client terminated.");

	}

}
