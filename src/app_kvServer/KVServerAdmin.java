package app_kvServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import app_kvServer.KVServerAdminThread;
import app_kvServer.KVServerReplicationScheduler;
import app_kvServer.KVServerThread;

public class KVServerAdmin extends Thread {

	private int port = 3000;
	private String nodeName = "";
	ServerSocket serverSocket = null;

	static Logger logger = Logger.getLogger(KVServerAdmin.class);

	public KVServerAdmin(int port, String nodeName) {
		this.port = port;
		this.nodeName = nodeName;

	}

	public void run() {
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
				logger.error("I/O error: " + e);
			}

			new KVServerAdminThread(socket, port, nodeName).start();
		}
	}
}
