package app_ecsServer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import app_ecsClient.ECSServerCommunicator;
import common.messages.KVAdminMessage;
import common.messages.KVAdminMessage.Command;
import common.messages.impl.KVAdminMessageImpl;

public class ECSServerLibrary {
	
	
	
	public static void launchProcess(String ipAddress, String port) {
	    try{
	        JSch jsch=new JSch();  

	        String host="aynesh@"+ipAddress;
	        String user=host.substring(0, host.indexOf('@'));
	        String privateKey="/home/aynesh/.ssh/id_rsa";
	        host=host.substring(host.indexOf('@')+1);
	        
	        Session session=jsch.getSession(user, host, 22);
	        jsch.addIdentity(privateKey);
	        java.util.Properties config = new java.util.Properties(); 
	        config.put("StrictHostKeyChecking", "no");
	        session.setConfig(config);
	        session.connect();

	        String command="java -jar /home/aynesh/Desktop/Praktikum/gr6/ms2-server.jar "+port+" 3 LFU";

	        Channel channel=session.openChannel("exec");
	        ((ChannelExec)channel).setCommand(command);

	        InputStream commandOutput = channel.getInputStream();

	        //channel.setOutputStream(System.out);

	        ((ChannelExec)channel).setErrStream(System.err);

	        InputStream in=channel.getInputStream();

	        channel.connect();

	        byte[] tmp=new byte[1024];
	        while(true){
	          while(in.available()>0){
	            int i=in.read(tmp, 0, 1024);
	            if(i<0)break;
	            System.out.print(new String(tmp, 0, i));
	          }
	          if(channel.isClosed()){
	            if(in.available()>0) continue; 
	            System.out.println("exit-status: "+channel.getExitStatus());
	            break;
	          }
	        }
	        

			//channel.sendSignal("2"); // CTRL + C - interrupt
			channel.sendSignal("2 "); // KILL
	        channel.disconnect();
	        session.disconnect();
	      }
	      catch(Exception e){
	        System.out.println(e);
	      }
	}

	public static Map<String, Node> readConfigFile() {
		String fileName = "ecs.config" ;
		Map<String, Node> serverConfig = new HashMap();
		
	    FileReader fileReader;
		try {
			fileReader = new FileReader(fileName);
		    BufferedReader bufferedReader = new BufferedReader(fileReader);
		    String readLine = bufferedReader.readLine();
		    while(readLine != null) {
		    	String values[] = readLine.split(" ");
		    	Node node = new Node(values[0], values[1], values[2]);
		    	serverConfig.put(values[0], node);
		    	readLine = bufferedReader.readLine();
		    }
		    
		    bufferedReader.close();
		
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return serverConfig;
	}
	
	public static void sendStart() {
		try {
			ECSServerCommunicator client = null; 
			client = new ECSServerCommunicator("127.0.0.1", 3000);
			client.connect();
			KVAdminMessageImpl msg = new KVAdminMessageImpl();
			msg.setCommand(KVAdminMessage.Command.START);
			KVAdminMessage recd= client.sendMessage(msg);
			if(recd.getCommand()==Command.START_SUCCESS) {
				System.out.println("Sent to KV Server");
			}
			
			client.disconnect();
		} catch (UnknownHostException e) {
			System.out.println("Unknown host. Unable to establish connection.");
		} catch (IOException e) {
			System.out.println("Unable to establish connection.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
