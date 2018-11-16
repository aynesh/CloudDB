package app_kvServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import common.messages.KVAdminMessage;
import common.messages.KVAdminMessageManager;
import common.messages.KVAdminMessage.Command;
import common.messages.impl.KVAdminMessageImpl;

public class KVServerAdminThread extends Thread {
	
	private int port = 3000;

	public KVServerAdminThread(int port) {
		
		this.port = port;

	}
	
	public void run() {
		ServerSocket serverSocket = null;
		Socket socket = null;
		try {
			serverSocket = new ServerSocket(this.port);
		} catch (IOException e) {
			e.printStackTrace();

		}
		while (true) {
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				System.out.println("I/O error: " + e);
			}

			InputStream inp = null;
			BufferedReader brinp = null;
			DataOutputStream out = null;

			try {
				inp = socket.getInputStream();
				brinp = new BufferedReader(new InputStreamReader(inp));
				out = new DataOutputStream(socket.getOutputStream());

			} catch (IOException e) {
				return;
			}
			while (true) {
				try {
					KVAdminMessage inpMsg = KVAdminMessageManager.receiveKVAdminMessage(inp);
					KVAdminMessageImpl outMsg = new KVAdminMessageImpl();

					switch (inpMsg.getCommand()) {
					case START:
						try {
							outMsg.setCommand(Command.START_SUCCESS);
						} catch (Exception e) {

						}
						break;

					default:
						break;
					}
					KVAdminMessageManager.sendKVAdminMessage(outMsg, out);
					System.out.println("Started Accepting commands");
				} catch (Exception e) {
					break;
				}
			}

		}
	}
}
