package common.messages;

import app.common.Node;

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
        PING_SUCCESS
    }
    
    public int getNumberOfNodes();
    
    public int getCacheSize();
    
    public String getCacheType();
    
    public Node getServer();
    
    public Node getTransferServer();
    
    public String getTransferStartKey();
    
    public String getTransferEndKey();
    
    public Command getCommand();
    
    public Node[] getMetaData();    

}
