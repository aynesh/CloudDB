package app_kvServer;

import java.io.IOException;

public class server {
	public static void main(String[] args) throws IOException
	{
		new KVServer("node3", 50002, 4000, 1, "FIFO"); 	
		
	}
}
