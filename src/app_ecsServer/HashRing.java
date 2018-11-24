package app_ecsServer;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.DatatypeConverter;

public class HashRing {
	
	private Map<BigInteger, Node> map = new TreeMap<BigInteger, Node>();
	
	public void addNode(Node node) {
		try {
			String key = HashRing.getMD5Hash(node.getIpAndPort());
			node.setEndRange(key);
			BigInteger bi = new BigInteger(key, 16);
			map.put(bi, node);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public void removeNode(Node node) {
		try {
			System.out.println("node");
			System.out.println(node);
			String key = HashRing.getMD5Hash(node.getIpAndPort()); // Problem here
			BigInteger bi = new BigInteger(key, 16);
			printMetaData(getMetaData());
			System.out.println("bi");
			System.out.println(bi);
			System.out.println("key");
			System.out.println(key);
			System.out.println("map");
			System.out.println(map);
			map.remove(bi);
		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		}
	}
	
	public void removeAll() {
		map.clear();
	}
	
	public void clearAndSetMetaData(Node[] metaData) {
		map.clear();
		for(Node node: metaData) {
			BigInteger bi = new BigInteger(node.getEndRange(), 16);
			map.put(bi, node);
		}
	}
	
/*	
	public static boolean checkKeyRange(String key, String startKey, String endKey) {
		String keyHash=null;
		try {
			keyHash = HashRing.getMD5Hash(key);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(keyHash==null) {
			return false;
		}
		System.out.println("keyHash: "+keyHash);
		BigInteger keyBi = new BigInteger(keyHash, 16);
		BigInteger startKeyBi = new BigInteger(startKey, 16);
		BigInteger endKeyBi = new BigInteger(endKey, 16);
		System.out.println("startBi: ");
		if(keyBi.compareTo(startKeyBi) == -1 && keyBi.compareTo(endKeyBi) == 1) {
			return true;
		}
		
		return false;
	}*/
	
	public Node getPrevNode(Node node) {
		int i=0;
		Node prevNode=null;
		for(Map.Entry<BigInteger, Node> entry : map.entrySet()) {
			if(i!=0 && entry.getValue().getName() == node.getName()) {
				return prevNode;
			}
			prevNode = entry.getValue();
			i++;
		}
		return prevNode; 
	}
	
	public Node getNextNode(Node node) {
		int i=0;
		Node nextNode=null;
		boolean nextNodeFlag = false;
		for(Map.Entry<BigInteger, Node> entry : map.entrySet()) {
			if(i==0) {
				nextNode = entry.getValue();
			}
			if(nextNodeFlag) {
				return entry.getValue();
			}
			if(entry.getValue().getName() == node.getName()) {
				nextNodeFlag = true;
			}
			i++;
		}
		return nextNode;
	}
	
	public Node getNode(String key) throws NoSuchAlgorithmException {
		String keyHash = HashRing.getMD5Hash(key);
		BigInteger bi = new BigInteger(keyHash, 16);
		Node firstNode = null;
		int i=0;
		for(Map.Entry<BigInteger, Node> entry : map.entrySet()) {
			BigInteger mapKey = entry.getKey();
			Node node = entry.getValue();
			if(mapKey.compareTo(bi) == 1 ) {
				return node;
			}
			if(i==0) {
				firstNode = node;
			}
			i++;
		}
		return firstNode; //Circular 
	}
	
	public Node[] getMetaData() {
		Node[] nodes = new Node[map.size()];
		Node prevNode = null;
		int i=0;
		for(Map.Entry<BigInteger, Node> entry : map.entrySet()) {
			Node node = entry.getValue();
			
			if(prevNode!=null) {
				node.setStartRange(prevNode.getEndRange());
			}
			
			nodes[i++] = node;
			prevNode = node;
		}
		if(map.size()> 0) {
			nodes[0].setStartRange(nodes[map.size()-1].getEndRange());
		}
		return nodes;
	}
	
	public static void printMetaData(Node nodes[]) {
		for(Node node:nodes) {
			System.out.println(node.toString());
		}
	}
	
	public static String getMD5Hash(String input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(input.getBytes());
		byte[] digest = md.digest();
		String myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
		return myHash;
	}

}
