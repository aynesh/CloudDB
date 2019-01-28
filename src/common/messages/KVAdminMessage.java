package common.messages;

import java.time.LocalDateTime;
import java.util.Arrays;

import app.common.Node;
import common.messages.KVAdminMessage.Command;

public interface KVAdminMessage {
	
    public enum Command {
        START,
        STOP,
        SHUTDOWN,
        INIT_SERVICE,
        ADD_NODE,
        REMOVE_NODE,
        INIT_SERVICE_SUCCESS,
        INIT_SERVICE_FAIL,
        START_SUCCESS,
        STOP_SUCCESS,
        ADD_NODE_SUCCESS,
        REMOVE_NODE_SUCCESS,
        SHUTDOWN_SUCCESS,
        SERVER_WRITE_LOCK,
        SERVER_WRITE_UNLOCK, 
        TRANSFER,
        TRANSFER_AND_SHUTDOWN,
        TRANSFER_SUCCESS,
        EXCEPTION,
        GET_META_DATA,
        META_DATA_UPDATE,
        META_DATA_UPDATE_SUCCESS,
        PING,
        PING_FORWARD,
        PING_FAILURE,
        PING_SUCCESS,
        REPLICATE,
        REPLICATE_SUCCESS,
        DELETE_REPLICATED_FILES,
        DELETE_REPLICATED_FILES_SUCCESS, GET, PUT, GET_SUCCESS, SERVER_NOT_RESPONSIBLE, PUT_SUCCESS, CHANGE_CONSISTENCY_LEVELS,
    }
    
    public int getNumberOfNodes();
    
    public int getCacheSize();
    
   
    
    public Node getTransferServer();
    
    public String getValue();
    public void setValue(String s);
    public void setTimestamp(LocalDateTime t);
    public LocalDateTime getTimestamp();
    public Command getCommand();
    public void setCommand(Command cmd);
    
    public String getECSIP();
	public void setECSIP(String eCSIP);
	public int getPort();
	public void setPort(int port);

	
	
	public void setNumberOfNodes(int numberOfNodes) ;
	
	public void setCacheSize(int cacheSize) ;
	public String getCacheType() ;
	public void setCacheType(String cacheType) ;
	public Node getServer();
	public void setServer(Node server) ;
	public Node[] getMetaData() ;
	public void setMetaData(Node[] metaData);
	
	public String toString() ;
	public String getTransferStartKey() ;
	public void setTransferStartKey(String transferStartKey);
	public String getTransferEndKey() ;
	public void setTransferEndKey(String transferEndKey);
	public void setTransferServer(Node transferServer);

	public void setKey(String key);

	public String getKey();

	public int getReadStats();

	public int getWriteStats();

	public void setReadStats(int readStats);

	public void setWriteStats(int writeStats);

	public void setReadConsistencyLevel(int rcl);

	public void setWriteConsistencyLevel(int wcl);

	public int getReadConsistencyLevel();

	public int getWriteConsistencyLevel();

}
