package testing;

import app_kvServer.KVServer;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	
    static {
        try {
        	Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                	new KVServer("node3", 50002, 4000, 1, "FIFO","/home/aynesh/node3",3);
                }
            });
        	thread.start();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        TestSuite clientSuite = new TestSuite("Basic Storage ServerTest-Suite");
        clientSuite.addTestSuite(ConnectionTest.class);
        clientSuite.addTestSuite(InteractionTest.class);
        clientSuite.addTestSuite(AdditionalTest.class);
        clientSuite.addTestSuite(CacheTest.class);
        return clientSuite;
    }

}
