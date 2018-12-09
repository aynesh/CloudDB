package app.common;

import java.io.Serializable;

public class Node implements Serializable {
	/**
	 * 
	 */
	@Override
	public String toString() {
		return "Node [name=" + name + ", port=" + port + ", ipAddress=" + ipAddress + ", startWriteRange=" + startWriteRange
				+ ", endWriteRange=" + endWriteRange + ", startReadRange=" + startReadRange + "]";
	}
	private String name;
	private String port;
	private String ipAddress;
	private String startWriteRange;
	private String endWriteRange;
	private String location;
	private String adminPort;
	private String userName;
	private String storagePath;
	private String startReadRange;
	
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
	public String getEndWriteRange() {
		return endWriteRange;
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
	public String getStartWriteRange() {
		return startWriteRange;
	}
	public void setEndWriteRange(String endRange) {
		this.endWriteRange = endRange;
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
	public void setStartWriteRange(String startRange) {
		this.startWriteRange = startRange;
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
	public String getStartReadRange() {
		return startReadRange;
	}
	public void setStartReadRange(String startRange) {
		this.startReadRange = startRange;
	}
}
