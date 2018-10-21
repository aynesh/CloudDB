package ui;

import java.io.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

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
			sock = new Socket(ip, Integer.parseInt(port));
			in = sock.getInputStream();
			out = sock.getOutputStream();
			
			byte[] recvBytes = new byte[131072];
			in.read(recvBytes);
			System.out.print("Server> ");
			for(int i=0;recvBytes[i]!='\n';i++) {
				System.out.print((char)recvBytes[i]);
			}
			System.out.println();
			
		} catch (UnknownHostException e) {
			System.out.println("Unknown host. Unable to establish connection.");
			logger.error("Unknown host. Unable to establish connection.");

		} catch (IOException e) {
			System.out.println("Unable to establish connection.");
			logger.error("Unable to establish connection.");

		}
	}
	public void disconnect() {
		try {
	
			in.close();
			out.close();
			sock.close();
			System.out.println("Diconnected from server.");
		} catch (IOException e) {
			System.out.println("Diconnect failed.");

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void send(String tokens) {
	
		if(sock.isClosed()) {
			System.out.println("No open connection to server.");
		}
		else {
		try {
			tokens = tokens + "\r";
			out.write(tokens.getBytes());
			out.flush();
			
			byte[] recvBytes = new byte[131072];
			in.read(recvBytes);
			System.out.print("Server> ");
			for(int i=0;recvBytes[i]!='\n';i++) {
				System.out.print((char)recvBytes[i]);
			}
			System.out.println();
		} catch (IOException e) {
			
		} 
		}

	}
	
	public void setLevel(String level) {
		logger.info("Previous level:"+logger.getLevel());
		logger.setLevel(Level.toLevel(level));
		logger.info("New level:"+logger.getLevel());
		
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
		if(sock.isConnected()) {
			disconnect();
		}
		System.out.println("Client terminated.");
		
	}
}
