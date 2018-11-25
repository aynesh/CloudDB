package app.common;

import java.io.Serializable;

public class Node implements Serializable {
	/**
	 * 
	 */
	@Override
	public String toString() {
		return "Node [name=" + name + ", port=" + port + ", ipAddress=" + ipAddress + ", startRange=" + startRange
				+ ", endRange=" + endRange + "]";
	}
	private String name;
	private String port;
	private String ipAddress;
	private String startRange;
	private String endRange;
	private String location;
	private String adminPort;
	private String userName;
	private String storagePath;
	public Node() {
		
	}
	public Node(String name, String port, String ipAddress) {
		super();
		this.name = name;
		this.port = port;
		this.ipAddress = ipAddress;
	}
	public Node(String name, String userName, String ipAddress, String port, String adminPort, String location, String storagePath) {
		super();
		this.name = name;
		this.port = port;
		this.ipAddress = ipAddress;
		this.location = location;
		this.adminPort = adminPort;
		this.userName = userName;
		this.storagePath = storagePath;
	}
	public String getEndRange() {
		return endRange;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public String getIpAndPort() {
		return this.getIpAddress()+":"+this.getPort();
	}
	public String getName() {
		return name;
	}
	public String getPort() {
		return port;
	}
	public String getStartRange() {
		return startRange;
	}
	public void setEndRange(String endRange) {
		this.endRange = endRange;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public void setStartRange(String startRange) {
		this.startRange = startRange;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getAdminPort() {
		return adminPort;
	}
	public void setAdminPort(String adminPort) {
		this.adminPort = adminPort;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getStoragePath() {
		return storagePath;
	}
	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}
}
