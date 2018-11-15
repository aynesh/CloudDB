package common.messages;

public interface KVAdminMessage {
	
    public enum Command {
        START, 			/* START - request */
        STOP,
        SHUTDOWN
    }

}
