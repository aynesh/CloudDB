package common.messages.impl;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;

import app.common.Node;
import common.messages.KVAdminMessage;

public class KVAdminMessageImpl implements KVAdminMessage, Serializable {
	
	private Command command;
	private int numberOfNodes;
	private int cacheSize;
	private String cacheType;
	private Node server;
	private Node metaData[];
	private String transferStartKey;
	private String transferEndKey;
	private Node transferServer;
	private String ECSIP;
	private int port;
	private String value;
	private LocalDateTime timestamp;
	private int readStats;
	private int writeStats;
	private String key;
	
	public int getReadStats() {
		return readStats;
	}
	public void setReadStats(int readStats) {
		this.readStats = readStats;
	}
	public int getWriteStats() {
		return writeStats;
	}
	public void setWriteStats(int writeStats) {
		this.writeStats = writeStats;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	public String getECSIP() {
		return ECSIP;
	}
	public void setECSIP(String eCSIP) {
		ECSIP = eCSIP;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public Command getCommand() {
		return command;
	}
	public void setCommand(Command command) {
		this.command = command;
	}
	public int getNumberOfNodes() {
		return numberOfNodes;
	}
	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}
	public int getCacheSize() {
		return cacheSize;
	}
	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}
	public String getCacheType() {
		return cacheType;
	}
	public void setCacheType(String cacheType) {
		this.cacheType = cacheType;
	}
	public Node getServer() {
		return server;
	}
	public void setServer(Node server) {
		this.server = server;
	}
	public Node[] getMetaData() {
		return metaData;
	}
	public void setMetaData(Node[] metaData) {
		this.metaData = metaData;
	}
	
	
	@Override
	public String toString() {
		return "KVAdminMessageImpl [command=" + command + ", numberOfNodes=" + numberOfNodes + ", cacheSize="
				+ cacheSize + ", cacheType=" + cacheType + ", server=" + server + ", metaData="
				+ Arrays.toString(metaData) + ", transferStartKey=" + transferStartKey + ", transferEndKey="
				+ transferEndKey + ", transferServer=" + transferServer + ", ECSIP=" + ECSIP + ", port=" + port
				+ ", value=" + value + ", timestamp=" + timestamp + ", readStats=" + readStats + ", writeStats="
				+ writeStats + "]";
	}
	public String getTransferStartKey() {
		return transferStartKey;
	}
	public void setTransferStartKey(String transferStartKey) {
		this.transferStartKey = transferStartKey;
	}
	public String getTransferEndKey() {
		return transferEndKey;
	}
	public void setTransferEndKey(String transferEndKey) {
		this.transferEndKey = transferEndKey;
	}
	public Node getTransferServer() {
		return transferServer;
	}
	public void setTransferServer(Node transferServer) {
		this.transferServer = transferServer;
	}
	@Override
	public void setKey(String key) {
		this.key = key;
	}
	@Override
	public String getKey() {
		return key;
	}



}
