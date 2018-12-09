package common.messages;

import app.common.Node;

public interface KVMessage {

    public enum StatusType {
        GET, 			/* Get - request */
        GET_ERROR, 		/* requested tuple (i.e. value) not found */
        GET_SUCCESS, 	/* requested tuple (i.e. value) found */
        PUT, 			/* Put - request */
        PUT_SUCCESS, 	/* Put - request successful, tuple inserted */
        PUT_UPDATE, 	/* Put - request successful, i.e. value updated */
        PUT_ERROR, 		/* Put - request not successful */
        DELETE, 		/* Delete - request */
        DELETE_SUCCESS, /* Delete - request successful */
        DELETE_ERROR, 	/* Delete - request successful */
        SERVER_STOPPED,
        SERVER_NOT_RESPONSIBLE,
        SERVER_WRITE_LOCK,
        COPY, 
        COPY_SUCCESS,
        COPY_ERROR,
        DELETE_REPLICA_COPY, 
        DELETE_REPLICA_COPY_SUCCESS,
        DELETE_REPLICA_COPY_ERROR,
    }

    public enum DataType {
    	ORIGINAL,
    	REPLICA_COPY
    }
    
    /**
     * @return the key that is associated with this message,
     * null if not key is associated.
     */
    public String getKey();

    /**
     * @return the value that is associated with this message,
     * null if not value is associated.
     */
    public String getValue();

    /**
     * @return a status string that is used to identify request types,
     * response types and error types associated to the message.
     */
    public StatusType getStatus();
    
    public Node[] getMetaData();
    
    public void setMetaData(Node nodes[]);

	public void setKey(String key);

	public void setStatus(StatusType type);

	public void setValue(String string);
	
	public DataType getDataType();
	
	public void setDataType(DataType dataType);

}


