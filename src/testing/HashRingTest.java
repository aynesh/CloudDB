package testing;

import java.io.File;
import java.security.NoSuchAlgorithmException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import app_ecsServer.HashRing;
import app_ecsServer.Node;
import datastore.DataManager;
import junit.framework.TestCase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HashRingTest extends TestCase {

	@Test
	public void testFIFOCache() throws NoSuchAlgorithmException {
		Node node1 = new Node("node1", "127.0.0.1", "50000");
		Node node2 = new Node("node2", "127.0.0.1", "50006");
		Node node3 = new Node("node3", "127.0.0.1", "50002");
		HashRing hashRing = new HashRing();
		hashRing.addNode(node1);
		hashRing.addNode(node2);
		hashRing.addNode(node3);
		System.out.println(hashRing.getNode("hello").getName());
		System.out.println(hashRing.getNode("hello").getName());
		System.out.println(hashRing.getNode("google").getName());
		System.out.println(hashRing.getNode("facebook").getName());
		System.out.println(hashRing.getNode("India").getName());
		System.out.println(hashRing.getNode("Pakistan").getName());
		System.out.println(hashRing.getNode("Japan").getName());
		System.out.println(hashRing.getNode("Canada").getName());
		System.out.println(hashRing.getNode("sa98hgq98hasiufge").getName());
		System.out.println(hashRing.getNode("as3r2r").getName());
		System.out.println(hashRing.getNode("2342rsf4t").getName());
		System.out.println(hashRing.getNode("sdff2345rwfs").getName());
		System.out.println(hashRing.getNode("ewwer32rf4325rwe").getName());
		System.out.println(hashRing.getNode("23422342").getName());
		System.out.println(hashRing.getNode("234423r23rw").getName());
		System.out.println(hashRing.getMetaData()[0].toString());
		System.out.println(hashRing.getMetaData()[1].toString());
		System.out.println(hashRing.getMetaData()[2].toString());
		HashRing hashRing1 = new HashRing();
		hashRing1.addNode(node1);
		System.out.println(hashRing1.getMetaData()[0].toString());
	}
	
	@Test
	public void testFIFOCache2() throws NoSuchAlgorithmException {
		Node node1 = new Node("node2", "127.0.0.1", "50001");
		HashRing hashRing = new HashRing();
		hashRing.addNode(node1);
		System.out.println("----------------");
		System.out.println(hashRing.getNode("hello"));;
		HashRing hashRing2 = new HashRing();
		hashRing2.clearAndSetMetaData(hashRing.getMetaData());
		System.out.println(hashRing2.getNode("hello"));;
	}
	
	@Test
	public void testHashRing() {
		System.out.println("------ List Files ------");
		File files[] = DataManager.getAllTextFiles("node1");
		for(int i=0;i<files.length;i++) {
			System.out.println("inside loop : "+files[i].getName());
		}
		System.out.println("------ End of Files ------");
	}
	

}
	
