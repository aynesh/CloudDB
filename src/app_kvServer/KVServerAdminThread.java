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
	private String nodeName = "";

	public KVServerAdminThread(int port, String nodeName) {
		
		this.port = port;
		this.nodeName = nodeName;
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
							KVServer.serveClients = true;
							KVServer.metaData.clearAndSetMetaData(inpMsg.getMetaData());
							outMsg.setCommand(Command.START_SUCCESS);
						} catch (Exception e) {

						}
						System.out.println("Started Accepting commands");
						break;
					case STOP:
						try {
							KVServer.serveClients = false;
							outMsg.setCommand(Command.STOP_SUCCESS);
						} catch (Exception e) {

						}
						System.out.println("Stopped Accepting commands");
						break;
					case SHUTDOWN:
						try {
							KVServer.serveClients = false;
							outMsg.setCommand(Command.SHUTDOWN_SUCCESS);
						} catch (Exception e) {

						}
						System.out.println("Shutdown initiated !");
						break;

					default:
						break;
					}
					KVAdminMessageManager.sendKVAdminMessage(outMsg, out);
					if(inpMsg.getCommand()==Command.SHUTDOWN) {
						System.out.println("Shutdown In Progress !");
						System.exit(0);
					}
				} catch (Exception e) {
					break;
				}
			}

		}
	}
}
