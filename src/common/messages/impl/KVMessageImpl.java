package common.messages.impl;

import java.io.Serializable;
import java.time.LocalDateTime;

import app.common.Node;
import common.messages.KVMessage;

public class KVMessageImpl implements KVMessage,Serializable {

	private String key;
	private String value;
	private Node[] metaData;
	private StatusType type;
	private DataType dataType;
	private LocalDateTime timestamp;
	
	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

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

	@Override
	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	@Override
	public void setTimestamp(LocalDateTime date) {
		timestamp = date;
		
	}
	

}
