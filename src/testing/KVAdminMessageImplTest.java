package testing;

import static org.junit.Assert.*;

import org.junit.Test;

import common.messages.KVAdminMessage.Command;
import common.messages.KVMessage.StatusType;
import common.messages.impl.KVAdminMessageImpl;

public class KVAdminMessageImplTest
{

	@Test
	public void test()
	{
		KVAdminMessageImpl messageImpl = new KVAdminMessageImpl();
		messageImpl.setCommand(Command.START);
		
		assertNotNull(messageImpl.getCommand());
	}

}
