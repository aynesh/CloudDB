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
			String key = HashRing.getMD5Hash(node.getIpAndPort());
			BigInteger bi = new BigInteger(key, 16);
			map.remove(bi);
		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		}
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
	
	public static String getMD5Hash(String input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(input.getBytes());
		byte[] digest = md.digest();
		String myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
		return myHash;
	}

}
