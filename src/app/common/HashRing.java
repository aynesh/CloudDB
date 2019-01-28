package app.common;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import app_kvServer.KVServer;

public class HashRing {
	
	private Map<BigInteger, Node> map = new TreeMap<BigInteger, Node>();
	
	private int replicationFactor=0;
	
	static Logger logger = Logger.getLogger(HashRing.class);
	
	public HashRing(int replicationFactor) {
		this.replicationFactor = replicationFactor;
	}
	
	public HashRing() {
		
	}
	
	/**
	 * @param node - New Node to be added
	 */
	public void addNode(Node node) {
		try {
			String key = HashRing.getMD5Hash(node.getIpAndPort());
			node.setEndWriteRange(key);
			BigInteger bi = new BigInteger(key, 16);
			map.put(bi, node);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public Node getNodeObject(String nodeName) {
		for(Map.Entry<BigInteger, Node> entry : map.entrySet()) {
			if(entry.getValue().getName().equals(nodeName)) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	/**
	 * @param node - Node to be removed.
	 */
	public void removeNode(Node node) {
		try {
			logger.info("node");
			logger.info(node);
			String key = HashRing.getMD5Hash(node.getIpAndPort()); // Problem here
			BigInteger bi = new BigInteger(key, 16);
			logger.info(getMetaData());
			logger.info("bi");
			logger.info(bi);
			logger.info("key");
			logger.info(key);
			logger.info("map");
			logger.info(map);
			map.remove(bi);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e);
		}
	}
	
	/**
	 * Remove all the Nodes.
	 */
	public void removeAll() {
		map.clear();
	}
	
	/**
	 * @param metaData-To clear and reinitialize using Node array
	 */
	public void clearAndSetMetaData(Node[] metaData) {
		map.clear();
		for(Node node: metaData) {
			BigInteger bi = new BigInteger(node.getEndWriteRange(), 16);
			map.put(bi, node);
		}
	}
	
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
		BigInteger keyBi = new BigInteger(keyHash, 16);
		BigInteger startKeyBi = new BigInteger(startKey, 16);
		BigInteger endKeyBi = new BigInteger(endKey, 16);
		System.out.println("DEBUG: Sign: keyBi: "+keyBi.signum()+" startKey: "+startKeyBi.signum()+ " endKey: "+endKeyBi.signum());
		System.out.println(keyBi+" startKey");
		System.out.println(startKeyBi+" startKeyBi");
		System.out.println(endKeyBi+" endKeyBi");
		if( (startKeyBi.compareTo(keyBi) == 1 && keyBi.compareTo(endKeyBi) == -1) || ( keyBi.compareTo(endKeyBi) == 0 ||  keyBi.compareTo(startKeyBi) == 0) ) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * @param node The node for which previous node to be found.
	 * @return
	 */
	public Node getPrevNode(Node node) {
		int i=0;
		Node prevNode=null;
		for(Map.Entry<BigInteger, Node> entry : map.entrySet()) {
			if(i!=0 && entry.getValue().getName().equals(node.getName())) {
				return prevNode;
			}
			prevNode = entry.getValue();
			i++;
		}
		return prevNode; 
	}
	
	
	/**
	 * @param node The node for which previous nodes to be found.
	 * @return
	 */
	public Node[] getPrevNodes(Node node, int size) {
		if(size>=map.size()) {
			return null;
		}
		Node[] prevNodes= new Node[size];
		Node prevNode = this.getPrevNode(node);
		for(int i=0;i<size;i++) {
			prevNodes[i] = prevNode;
			prevNode = this.getPrevNode(prevNode);
		}
		return prevNodes; 
	}
	
	/**
	 * @param node The node for which previous node to be found.
	 * @return
	 */
	public Node getPrevNode(String nodeName) {
		int i=0;
		Node prevNode=null;
		for(Map.Entry<BigInteger, Node> entry : map.entrySet()) {
			if(i!=0 && entry.getValue().getName().equals(nodeName)) {
				return prevNode;
			}
			prevNode = entry.getValue();
			i++;
		}
		return prevNode; 
	}
	
	public Node getNextNode(Node node) {
		if(node==null) {
			return null;
		}
		return this.getNextNode(node.getName());
	}
	
	/**
	 * @param node The node for which next node to be found.
	 * @return
	 */
	public Node getNextNode(String nodeName) {
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
			if(entry.getValue().getName().equals(nodeName)) {
				nextNodeFlag = true;
			}
			i++;
		}
		return nextNode;
	}
	
	/**
	 * @param node The node for which previous nodes to be found.
	 * @return
	 **/
	public Node[] getNextNodes(Node node, int size) {
		if(size>=map.size()) {
			return null;
		}
		Node[] nextNodes= new Node[size];
		Node nextNode = this.getNextNode(node);
		for(int i=0;i<size;i++) {
			nextNodes[i] = nextNode;
			nextNode = this.getNextNode(nextNode);
		}
		return nextNodes; 
	}
	
	
	
	public Node[] getNodesOfKey(String key) throws NoSuchAlgorithmException {
		
		int size = KVServer.replicationFactor+1;
		
		if(size>=map.size()) {
			return null;
		}
		Node[] nextNodes= new Node[size];
		
		Node nextNode = getNode(key);
		for(int i=0;i<size;i++) {
			nextNodes[i] = nextNode;
			nextNode = this.getNextNode(nextNode);
		}
		return nextNodes; 
	}
		
	
	/**
	 * @param key 
	 * @return The node responsible for the given the key.|
	 * @throws NoSuchAlgorithmException
	 */
	/**
	 * @param key
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public Node getNode(String key) throws NoSuchAlgorithmException {
		String keyHash = HashRing.getMD5Hash(key);
		BigInteger bi = new BigInteger(keyHash, 16);
		Node firstNode = null;
		int i=0;
		for(Map.Entry<BigInteger, Node> entry : map.entrySet()) {
			BigInteger mapKey = entry.getKey();
			Node node = entry.getValue();
			if(mapKey.compareTo(bi) == 1 || mapKey.compareTo(bi) == 0) {
				return node;
			}
			if(i==0) {
				firstNode = node;
			}
			i++;
		}
		return firstNode; //Circular 
	}
	
	/**
	 * @return Meta data in Node array form.
	 */
	public Node[] getMetaData() {
		Node[] nodes = new Node[map.size()];
		Node prevNode = null;
		int i=0;
		for(Map.Entry<BigInteger, Node> entry : map.entrySet()) {
			Node node = entry.getValue();
			
			if(prevNode!=null) {
				node.setStartWriteRange(prevNode.getEndWriteRange());
			}
			
			nodes[i++] = node;
			prevNode = node;
		}
		if(map.size()> 0) {
			nodes[0].setStartWriteRange(nodes[map.size()-1].getEndWriteRange());
		}
		// Set read range + 2 nodes
		for(i=0;i<nodes.length;i++) {
			nodes[i].setStartReadRange(nodes[(i-replicationFactor >= 0 ? i-replicationFactor : (nodes.length + (i-replicationFactor)) )%nodes.length].getStartWriteRange());
		}
		return nodes;
	}
	
	/**
	 * @param nodes Prints the meta data to console.
	 */
	public static void printMetaData(Node nodes[]) {
		for(Node node:nodes) {
			System.out.println(node.toString());
		}
	}
	
	public static String toString(Node nodes[]) {
		StringBuilder str = new StringBuilder();
		for(Node node:nodes) {
			str.append(node.toString());
		}
		return str.toString();
	}
	
	/**
	 * @param input
	 * @return Returns the MD5 hash for the given string.
	 * @throws NoSuchAlgorithmException
	 */
	public static String getMD5Hash(String input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(input.getBytes());
		byte[] digest = md.digest();
		String myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
		return myHash;
	}

	public boolean checkIfReplica(String key, Node node, int replicationFactor) throws NoSuchAlgorithmException {
		Node[] prevNodes = this.getPrevNodes(node, replicationFactor);
		for(int i=0; i<prevNodes.length ; i++) {
			if(prevNodes[i] != null && this.getNode(key).getName().equals(prevNodes[i].getName())) {
				return true; 
			}
		}
	
		return false;
	}
	
}
