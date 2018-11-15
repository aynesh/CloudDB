package app_ecsServer;

import java.io.InputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class ECSServer {

	public static void main(String[] args) {
	    try{
	        JSch jsch=new JSch();  

	        String host="aynesh@127.0.0.1";
	        String user=host.substring(0, host.indexOf('@'));
	        String privateKey="/home/aynesh/.ssh/id_rsa";
	        host=host.substring(host.indexOf('@')+1);
	        
	        Session session=jsch.getSession(user, host, 22);
	        jsch.addIdentity(privateKey);
	        /*
	        String xhost="127.0.0.1";
	        int xport=0;
	        String display=JOptionPane.showInputDialog("Enter display name", 
	                                                   xhost+":"+xport);
	        xhost=display.substring(0, display.indexOf(':'));
	        xport=Integer.parseInt(display.substring(display.indexOf(':')+1));
	        session.setX11Host(xhost);
	        session.setX11Port(xport+6000);
	        */
	        java.util.Properties config = new java.util.Properties(); 
	        config.put("StrictHostKeyChecking", "no");
	        session.setConfig(config);
	        // username and password will be given via UserInfo interface.
	        session.connect();

	        String command="ls";

	        Channel channel=session.openChannel("exec");
	        ((ChannelExec)channel).setCommand(command);

	        // X Forwarding
	        // channel.setXForwarding(true);

	        //channel.setInputStream(System.in);
	        channel.setInputStream(null);

	        //channel.setOutputStream(System.out);

	        //FileOutputStream fos=new FileOutputStream("/tmp/stderr");
	        //((ChannelExec)channel).setErrStream(fos);
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
	          try{Thread.sleep(1000);}catch(Exception ee){}
	        }
	        channel.disconnect();
	        session.disconnect();
	      }
	      catch(Exception e){
	        System.out.println(e);
	      }
	    }

}


