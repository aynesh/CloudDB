package app_ecsServer;

public class Node {
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
	public Node() {
		
	}
	public Node(String name, String port, String ipAddress) {
		super();
		this.name = name;
		this.port = port;
		this.ipAddress = ipAddress;
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
}
