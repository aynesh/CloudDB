package common.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;


public class KVMessageManager {
	
	static Logger logger = Logger.getLogger(KVMessageManager.class);
	
    /**
     * @param msg
     * @return
     * @throws IOException
     */
    public static byte[] marshall(KVMessage msg) throws IOException {
    	ObjectOutput oout = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		
			oout = new ObjectOutputStream(bos);
			oout.writeObject(msg);
			oout.flush();
		
		
		byte[] yourBytes = bos.toByteArray();
		return yourBytes;
    }
    
    /**
     * @param arr
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static KVMessage unmarshall(byte[] arr) throws IOException, ClassNotFoundException {
    	ByteArrayInputStream bis = new ByteArrayInputStream(arr);
    	ObjectInput in = null;
    	
    	  in = new ObjectInputStream(bis);
    	  Object o = in.readObject(); 
    	  return (KVMessage) o;
    	
    }

	/**
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static byte[] receive(InputStream in) throws IOException {
		byte[] recvBytes = new byte[131072];
		try {
			in.read(recvBytes);
		} catch (UnsupportedEncodingException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
		return recvBytes;
	}
	
	/**
	 * @param arr
	 * @param out
	 * @throws IOException
	 */
	public static void send(byte[] arr, OutputStream out) throws  IOException {
		try {
			out.write(arr);
			out.flush();
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * @param msg
	 * @param out
	 * @throws IOException
	 */
	public static void sendKVMessage(KVMessage msg, OutputStream out) throws IOException {
		send(marshall(msg),out);
		
	}

	/**
	 * @param in
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static KVMessage receiveKVMessage(InputStream in) throws IOException, ClassNotFoundException {
		KVMessage msg = unmarshall(receive(in));
		return msg;
	}
}
