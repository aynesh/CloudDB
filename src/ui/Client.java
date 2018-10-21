package ui;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Client {
	
	static Logger logger = Logger.getLogger(Client.class);
	
	Socket sock;
	InputStream in;
	OutputStream out;

	public void connect(String ip, String port) {
		try {
			//connect 131.159.52.2 50000
			logger.info("Attempting to establish connection to "+ip+" "+port);

			sock = new Socket(ip, Integer.parseInt(port));
			in = sock.getInputStream();
			out = sock.getOutputStream();
			
			byte[] recvBytes = new byte[131072];
			in.read(recvBytes);
			String recd = new String(recvBytes, "UTF-8").trim();
			System.out.print("Server> "+recd);
			logger.info("Server response: "+recd);

		} catch (UnknownHostException e) {
			System.out.println("Unknown host. Unable to establish connection.");
			logger.error("Unknown host. Unable to establish connection.");

		} catch (IOException e) {
			System.out.println("Unable to establish connection.");
			logger.error("Unable to establish connection.");

		}
	}
	public void disconnect() {
		if(sock == null || sock.isClosed()) {
			System.out.println("No open connection.");
			logger.error("No open connection.");
			return;
		}
		
		try {
	
			in.close();
			out.close();
			sock.close();
			System.out.println("Diconnected from server.");
			logger.info("Diconnected from server.");
		} catch (IOException e) {
			System.out.println("Diconnect failed.");
			logger.error("Failed to disconnect.");
		}
		
	}
	public void sendMessage(String tokens) {
	
		if(sock == null || sock.isClosed()) {
			logger.warn("Attempting to connect via closed socket.");
			System.out.println("No open connection to server.");
			
		}
		else {
		
			logger.info("Attempting to send message: "+tokens);
			tokens = tokens + "\r";
			send(tokens.getBytes());
			receive();

		}

	}
	
	public void setLevel(String level) {
		Level prvLevel = logger.getLevel();
		
		logger.setLevel(Level.toLevel(level));
		System.out.println("Changed log level from "+prvLevel+" to "+ logger.getLevel());
		logger.info("Changed log level from "+prvLevel+" to "+ logger.getLevel());
		
	}
	public void help() {
		System.out.println("Intended usage of available commands:");
		System.out.println("connect <address> <port> - Tries to establish a TCP- connection to the server at <address> and <port>.");
		System.out.println("disconnect - Tries to disconnect from server");
		System.out.println("send <message> - Tries to send <message> to connected server");
		System.out.println("logLevel <level> - Tries to set logger level to <level>");
		System.out.println("quit - quits the echo client");
		System.out.println("help - displays list of available commands");
	}

	public void invalidCommand() {
		System.out.println("Unknown command.");
		help();

		
	}
	public void terminate() {
		if(!sock.isClosed()) {
			disconnect();
		}
		System.out.println("Client terminated.");
		
	}
	
	public void send(byte[] arr) {
		try {
			out.write(arr);
			out.flush();
		} catch (IOException e) {
			System.out.println("Failed to send message.");
			logger.error("Failed to send message.");
		}
		
	}
	
	public byte[] receive() {
		byte[] recvBytes = new byte[131072];
		try {
			in.read(recvBytes);
			String recd;
			
				recd = new String(recvBytes, "UTF-8").trim();
				System.out.println("Server> "+recd);
				logger.info("Server response: "+recd);
			} catch (UnsupportedEncodingException e) {
				System.out.println("Failed to decode message.");
				logger.error("Failed to decode message.");
			} catch (IOException e) {
				System.out.println("Failed to receive response.");
				logger.error("Failed to receive response.");
			}	
		
		return recvBytes;		
	}
}
