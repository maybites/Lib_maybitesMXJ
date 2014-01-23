package ch.maybites.mxj.utils.kinect;

import java.io.*;
import java.net.*;
import java.util.*;

public class KinectTCPClientThread extends Thread {
 
	public String host = null;
	public int port = 0;
    protected Socket socket = null;
    protected boolean isInitialized = false;
    protected boolean isopen = false;
    protected KinectClientListener listener = null;
    
    protected BufferedReader bufferedReader;
  
    public KinectTCPClientThread(KinectClientListener _listener, String _host, int _port) throws IOException {
    	super("KinectClientThread");
        listener = _listener;
        host = _host;
        port = _port;
    }
    
    private boolean getConnected(boolean forceconnect){
    	if(socket == null || !socket.isConnected() || forceconnect){
    		isInitialized = false;
    		try {
    			listener.tcpevent("attempting connection... ");
    			if(socket != null)
    				socket.close();
    			//socket = new Socket(host, port, InetAddress.getByName("127.0.0.1"), localport);   	
    			socket = new Socket(host, port); //, InetAddress.getByName("127.0.0.1"), localport);   	
    			//socket.setReuseAddress(true);
    			listener.tcpevent("... connection established");
    		} catch (IOException e) {
    			listener.tcperror("error connecting: " + e.getMessage());
    		}
    	}
    	return (socket != null)?socket.isConnected(): false;
    }
 
    private boolean disconnect(){
    	if(socket != null){
    		isInitialized = false;
    		listener.tcpevent("trying to disconnect from host:" + host + " at port:"+port);
    		try {
       			listener.tcpevent("disconnecting...");
       			if(!socket.isClosed()){
       				socket.shutdownInput();
       				socket.shutdownOutput();
       				//socket.setSoLinger(true, 50);
    				socket.close();
       			}
    			socket = null;
    			listener.tcpevent("...disconnected");
    			return true;
    		} catch (IOException e) {
    			listener.tcperror("error disconnecting: " + e.getMessage());
    		}
    	}
    	return false;
    }
    
    public void close(){
    	isopen = false;
    	if(bufferedReader != null)
    		disconnect();
    	this.interrupt();
    }
    
    public void run() {
		listener.tcpevent("trying to connect to host:" + host + " at port:"+port);
    	isopen = true;
    	int linecounter = 0;
        while (isopen) {
    		if(getConnected(false)){
    			if(isInitialized){
    				listener.tcpevent("request frame");
    				writeMessage("requestframe[/TCP]");
    				listener.tcpevent(readMessage());
        		} else {
    				listener.tcpevent("request connectioninit");
            		writeMessage("connectioninit[/TCP]");
            		listener.tcpevent(readMessage());

           			listener.tcpevent("initialzed connection");
           			isInitialized = true;
       			}
        	}else{
               	try {
    				this.sleep(1000);
    			} catch (InterruptedException e) {;}       			
        	}
        }
        if(disconnect())
    		listener.tcpevent("No connection was established, therefore nothing to close");
     }
    
    private void writeMessage(String nachricht) {
    	try{
    		PrintWriter printWriter = new PrintWriter(
    				new OutputStreamWriter(socket.getOutputStream()));
    		printWriter.print(nachricht);
    		printWriter.flush();
		} catch (IOException e) {
			listener.tcperror(e.getMessage() + " -> closing connection");
			disconnect();
		}
   }

    private String readMessage(){
    	try{
    		bufferedReader = new BufferedReader(
    				new InputStreamReader(socket.getInputStream()));
    		char[] buffer = new char[200];
    		int anzahlZeichen = bufferedReader.read(buffer, 0, 200); // blockiert bis Nachricht empfangen
    		String nachricht = new String(buffer, 0, anzahlZeichen);
    		return nachricht;
    	} catch(java.lang.StringIndexOutOfBoundsException e){;
			listener.tcperror("transmission error -> closing connection");
			disconnect();
    	} catch (IOException e) {
			listener.tcperror(e.getMessage() + " -> disconnect connection");
			disconnect();
		}    	
    	return "disconnected";
    }

    public void delete(){
    	this.interrupt();
    	disconnect();
    }
     
 }