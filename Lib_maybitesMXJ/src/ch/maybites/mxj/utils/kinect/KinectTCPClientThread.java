package ch.maybites.mxj.utils.kinect;

import java.io.*;
import java.net.*;
import java.util.*;

public class KinectTCPClientThread extends Thread {
 
	public String host = null;
	public int port = 0;
	public int localport = 0;
    protected Socket socket = null;
    protected boolean isInitialized = false;
    protected boolean isopen = false;
    protected KinectClientListener listener = null;
  
    public KinectTCPClientThread(KinectClientListener _listener, String _host, int _port, int _localport) throws IOException {
    	super("KinectClientThread");
        listener = _listener;
        host = _host;
        port = _port;
        localport = _localport;
    }
    
    private boolean isConnected(){
    	if(socket == null || !socket.isConnected()){
    		try {
    			socket = new Socket(host, port, InetAddress.getByName("127.0.0.1"), localport);   	
    			listener.tcpevent("... connection established");
    		} catch (IOException e) {
    			listener.tcperror("error connecting: " + e.getMessage());
    		}
    	}
    	return (socket != null)?socket.isConnected(): false;
    }
    
    private void disconnect(){
    	if(socket != null && socket.isConnected()){
    		try {
    			socket.shutdownInput();
    			socket.shutdownOutput();
    			//socket.setSoLinger(true, 50);
    			socket.close();
    			socket = null;
    			listener.tcpevent("...disconnected");
    		} catch (IOException e) {
    			listener.tcperror("error disconnecting: " + e.getMessage());
    		}
    	}
    }
    
    public void close(){
    	isopen = false;
    }
    
    public void run() {
		listener.tcpevent("trying to connect to host:" + host + " at port:"+port);
    	isopen = true;
    	int linecounter = 0;
        while (isopen) {
        	if(isInitialized){
        		try {
        			writeMessage("requestframe[/TCP]");
            	
        			listener.tcpevent(readMessage());
                
        		} catch (IOException e) {
        			listener.tcperror(e.getMessage());
        			isInitialized = false;
        		}
        	} else {
        		if(isConnected()){
            		try {
            			writeMessage("request init");
            			listener.tcpevent(readMessage());

            			listener.tcpevent("initialzed connection");
            			isInitialized = true;
            		} catch (IOException e) {
            			listener.tcperror(e.getMessage());
            			isInitialized = false;
            		}
        		}else{
               		try {
    					this.sleep(1000);
    				} catch (InterruptedException e) {;}       			
        		}
        	}
        }
		listener.tcpevent("trying to disconnect from host:" + host + " at port:"+port);
        disconnect();
     }
    
    private void writeMessage(String nachricht) throws IOException {
    	if(socket.isConnected()){
    		PrintWriter printWriter = new PrintWriter(
    				new OutputStreamWriter(socket.getOutputStream()));
    		printWriter.print(nachricht);
    		printWriter.flush();
    	}
   }

    private String readMessage() throws IOException {
    	if(socket.isConnected()){
    		BufferedReader bufferedReader = new BufferedReader(
    				new InputStreamReader(socket.getInputStream()));
    		char[] buffer = new char[200];
    		int anzahlZeichen = bufferedReader.read(buffer, 0, 200); // blockiert bis Nachricht empfangen
    		String nachricht = new String(buffer, 0, anzahlZeichen);
    		return nachricht;
    	}
    	return "disconnected";
    }

    public void delete(){
    	this.interrupt();
    	disconnect();
    }
     
 }