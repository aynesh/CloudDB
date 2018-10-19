package ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;


public class Application {

	static Logger logger = Logger.getLogger(Application.class);
	
	public static void main(String[] args) throws IOException {
		logger.info("Hello World");     
		BufferedReader cons = new BufferedReader(new
		InputStreamReader(System.in));
		boolean quit = false;
		Client client = new Client();
		while (!quit) {
		System.out.print("Client> ");
		String input = cons.readLine();
		String[] tokens = input.trim().split("\\s+");
		if(tokens[0].equals("quit")) {
			quit = true;
		}
		else if(tokens[0].equals("connect")) {
			client.connect(tokens[1], tokens[2]);
		}
		else if(tokens[0].equals("send")) {
			client.send(tokens[1]);
		}
		else if(tokens[0].equals("disconnect")) {
			client.disconnect();
		}
		else if(tokens[0].equals("help")) {
			client.help();
		}
		else if(tokens[0].equals("logLevel")) {
			client.setLevel(tokens[1]);
			
		}
		else {
			client.invalidCommand();
		}
		
		}
		}

}
