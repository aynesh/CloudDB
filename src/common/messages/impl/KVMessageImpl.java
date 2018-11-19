package common.messages.impl;

import java.io.Serializable;

import app_ecsServer.Node;
import common.messages.KVMessage;

public class KVMessageImpl implements KVMessage,Serializable {

	String key;
	String value;
	Node[] metaData;
	StatusType type;
	
	public KVMessageImpl() {}
	
	public KVMessageImpl(String key, StatusType status) {
		this.key = key;
		this.type = status;
	}
	
	public KVMessageImpl(String key, String value, StatusType status) {
	
		this.value = value;
		this.key = key;
		this.type = status;
	}
	
	@Override
	public String getKey() {
		
		return this.key;
	}

	@Override
	public String getValue() {
		
		return this.value;
	}

	@Override
	public StatusType getStatus() {
		
		return this.type;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setStatus(StatusType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "KVMessageImpl [key=" + key + ", value=" + value + ", type=" + type + "]";
	}

	@Override
	public Node[] getMetaData() {
		return metaData;
	}

	@Override
	public void setMetaData(Node[] nodes) {
		this.metaData = nodes;
		
	}
	

}
