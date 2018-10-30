package ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

public class Application
{

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
					client.help();
				}
				else {
				client.connect(tokens[1], tokens[2]);
				}
			}
			else if (tokens[0].equals("send"))
			{
				client.sendMessage(String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length)));
			}
			else if (tokens[0].equals("disconnect"))
			{
				client.disconnect();
			}
			else if (tokens[0].equals("help"))
			{
				client.help();
			}
			else if (tokens[0].equals("logLevel"))
			{
				if(tokens.length<2) {
					System.out.println("Incorrect usage of command.");
					client.help();
				}
				else {
				client.setLevel(tokens[1]);
				}
			}
			else
			{
				client.invalidCommand();
			}
		}

		client.terminate();

	}

}
