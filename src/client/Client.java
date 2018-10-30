package client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Client {

	static Logger logger = Logger.getLogger(Client.class);

	Socket sock;
	InputStream in;
	OutputStream out;

	/**
	 * @param ip
	 * @param port This method creates connection to the given address represented
	 *             by 2 parameters: ip and port;
	 * @throws IOException, UnknownHostException 
	 */
	public String connect(String ip, String port) throws IOException, UnknownHostException {
		try {
			// connect 131.159.52.2 50000
			logger.info("Attempting to establish connection to " + ip + " " + port);

			sock = new Socket(ip, Integer.parseInt(port));
			in = sock.getInputStream();
			out = sock.getOutputStream();

			byte[] recvBytes = new byte[131072];
			in.read(recvBytes);
			String recd = new String(recvBytes, "UTF-8").trim();
			logger.info("Server response: " + recd);
			return recd;
			

		} catch (UnknownHostException e) {
			logger.error("Unknown host. Unable to establish connection.");
			throw e;
		} catch (IOException e) {
			logger.error("Unable to establish connection.");
			throw e;

		}
	}

	/**
	 * This method checks if there is a connection and if yes, it shuts down the
	 * active connection.
	 */
	public boolean disconnect() throws IOException {
		if (sock == null || sock.isClosed()) {
			logger.error("No open connection.");
			return false;
		}

		try {
			in.close();
			out.close();
			sock.close();
			logger.info("Diconnected from server.");
			return true;
		} catch (IOException e) {
			logger.error("Failed to disconnect.");
			throw e;
		}

	}

	/**
	 * @param tokens this method checks whether a socket is closed/is empty and
	 *               sends warning if yes, or sends the byte array otherwise. (calls
	 *               send method)
	 * @return 
	 * @throws IOException 
	 */
	public byte[] sendMessage(String tokens) throws IOException {

		if (sock == null || sock.isClosed()) {
			logger.warn("Attempting to connect via closed socket.");
			throw new IOException("No open connection to server.");
		} else {

			logger.info("Attempting to send message: " + tokens);
			tokens = tokens + "\r";
			send(tokens.getBytes());
			return receive();

		}


	}

	/**
	 * @param level This method sets the level of logging according to priority
	 */
	public void setLevel(String level) {
		Level prvLevel = logger.getLevel();

		logger.setLevel(Level.toLevel(level));
		logger.info("Changed log level from " + prvLevel + " to " + logger.getLevel());

	}

	/**
	 * This method checks if a socket is closed and if it's not, disconnects the
	 * connection with disconnect() method.
	 */
	public void terminate() throws IOException {
		if (!sock.isClosed()) {
			disconnect();
		}
	}

	/**
	 * @param arr this method sends an array of bytes via OutputStream
	 */
	public void send(byte[] arr) throws  IOException {
		try {
			out.write(arr);
			out.flush();
		} catch (IOException e) {
			logger.error("Failed to send message.");
			throw e;
		}
	}

	/**
	 * @return byte[] This method receives an array of bytes and creates a String
	 *         out of that array.
	 * @throws IOException 
	 */
	public byte[] receive() throws IOException {
		byte[] recvBytes = new byte[131072];
		try {
			in.read(recvBytes);
			String recd;

			recd = new String(recvBytes, "UTF-8").trim();
			logger.info("Server response: " + recd);
		} catch (UnsupportedEncodingException e) {
			logger.error("Failed to decode message.");
			throw e;
		} catch (IOException e) {
			logger.error("Failed to receive response.");
			throw e;
		}
		return recvBytes;
	}
}
