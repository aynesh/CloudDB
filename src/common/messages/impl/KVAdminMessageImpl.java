package common.messages.impl;

import java.io.Serializable;
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
				+ Arrays.toString(metaData) + "]";
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



}
