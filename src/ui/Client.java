package ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	
	Socket sock;
	InputStream in;
	OutputStream out;

	public void connect(String ip, String port) {
		try {
			sock = new Socket(ip, Integer.parseInt(port));
			in = sock.getInputStream();
			out = sock.getOutputStream();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void disconnect() {
		try {
	
			in.close();
			out.close();
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void send(String tokens) {
	
		try {
			
			out.write(tokens.getBytes());
			out.flush();
			
			byte[] recvByte = null;
			in.read(recvByte);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void setLevel(String level) {
		
	}
	public void help() {
		
	}
	public void invalidCommand() {
		
		
	}
}
