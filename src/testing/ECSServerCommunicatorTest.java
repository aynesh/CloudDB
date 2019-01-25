package testing;


import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import app.common.Node;
import app_ecsServer.ECSServerCommunicator;
import app_ecsServer.ECSServerLibrary;
import app_kvServer.KVServer;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ECSServerCommunicatorTest
{

	@Test
	public void connectTest()
	{

		new Thread(new Runnable() {
			@Override
			public void run()
			{
//				KVServer(String nodeName, int port, int adminPort, int cacheSize, String strategy, String path);
				String path = "D:\\TUM\\3rd Semester\\3. Lab Course - Cloud DB [IN0012, IN2106, IN4163]\\LatestVersion\\gr6\\path";
				KVServer newServer = new KVServer("newNode", 50001, 30000, 3, "FIFO", path, 0);
			}
		}).start();

		
		ECSServerCommunicator testCommunicator = new ECSServerCommunicator("127.0.0.1", 50001);
		UnknownHostException unknownHostEx = null;
		IOException ioException = null;
		Exception ex = null;

		try
		{
			testCommunicator.connect();
		}
		catch(UnknownHostException UHEx)
		{
			unknownHostEx = UHEx;
		}
		catch(IOException IOEx)
		{
			ioException = IOEx;
		}
		catch(Exception e)
		{
			ex = e;
		}
		
		assertNull(unknownHostEx);
		assertNull(ioException);
		assertNull(ex);
		
		
		Exception disconnectException = null;
		try
		{
			testCommunicator.disconnect();
		}
		catch(Exception e)
		{
			disconnectException = e;
		}
		
		assertNull(disconnectException);
		

	}

}
