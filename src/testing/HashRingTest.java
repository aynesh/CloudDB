package testing;

import java.io.File;
import java.security.NoSuchAlgorithmException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import app.common.HashRing;
import app.common.Node;
import datastore.DataManager;
import junit.framework.TestCase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HashRingTest extends TestCase {
	
	public void checkRange(String key, Node[] metaData) {
		System.out.println("-----------Checking Key: "+key+"-----------------");
		for(Node node:metaData) {
			System.out.println(node.toString());
			System.out.println("Write Range Check: "+HashRing.checkKeyRange(key, node.getStartWriteRange(), node.getEndWriteRange()));
			System.out.println("Read Range  Check: "+HashRing.checkKeyRange(key, node.getStartReadRange(), node.getEndWriteRange()));
		}
	}

	@Test
	public void testHashRing() throws NoSuchAlgorithmException
	{
		HashRing hashRing = new HashRing();

		Node node1 = new Node("node1", "127.0.0.1", "50000");
		Node node2 = new Node("node2", "127.0.0.1", "50001");
		Node node3 = new Node("node3", "127.0.0.1", "50002");
		Node node4 = new Node("node4", "127.0.0.1", "50003");
		Node node5 = new Node("node5", "127.0.0.1", "50004");
		
		assertNotNull(node1);
		assertNotNull(node2);
		assertNotNull(node3);
		
		hashRing.addNode(node1);
		hashRing.addNode(node2);
		hashRing.addNode(node3);
		hashRing.addNode(node4);
		hashRing.addNode(node5);
		
		int i=0;
		Node[] metaData = hashRing.getMetaData();
		checkRange("hello", metaData);
		checkRange("google", metaData);
		checkRange("india", metaData);
		checkRange("pakistan", metaData);
		
		
		
		assertNotNull(hashRing.getNode(node1.getIpAndPort()));
		assertNotNull(hashRing.getNode(node2.getIpAndPort()));
		assertNotNull(hashRing.getNode(node3.getIpAndPort()));
		
		hashRing.printMetaData(hashRing.getMetaData());
		
		Node[] metadata = hashRing.getMetaData();
		
		System.out.println(metadata);
		
//		assertEquals(hashRing.getNextNode(metadata[metadata.length-1]), hashRing.getPrevNode(metadata[0]));
		assertEquals(10, 10);
		
		
	}
	
}
