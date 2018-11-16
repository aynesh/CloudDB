package common.messages;

import app_ecsServer.Node;

public interface KVAdminMessage {
	
    public enum Command {
        START,
        STOP,
        SHUTDOWN,
        INIT_SERVICE,
        ADD_NODE,
        REMOVE_NODE,
        INIT_SERVICE_SUCCESS,
        START_SUCCESS
    }
    
    public int getNumberOfNodes();
    
    public int getCacheSize();
    
    public String getCacheType();
    
    public Node getServer();
    
    public Command getCommand();
    
    public Node[] getMetaData();    

}
