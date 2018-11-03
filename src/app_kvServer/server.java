package app_kvServer;

import java.io.IOException;

import common.messages.KVMessage;
import common.messages.KVMessage.StatusType;
import common.messages.impl.KVMessageImpl;
import client.KVStore;

public class server {
	public static void main(String[] args) throws IOException
	{
		new KVServer(50000, 10, "FIFO"); 
		/*KVMessage msg = new KVMessageImpl("a","b",StatusType.PUT);
		
		KVStore str = new KVStore("localhost",50000);
		byte[] arr = str.marshall(msg);
		KVMessage msg1 = str.unmarshall(arr);
		System.out.println(msg1.getValue());*/
		
		
	}
}
