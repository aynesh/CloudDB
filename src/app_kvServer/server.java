package app_kvServer;

import java.io.IOException;

public class server {
	public static void main(String[] args) throws IOException
	{
		new KVServer(50000, 3, "LFU"); 	
	}
}
