package ch.maybites.mxj.utils.kinect;

import java.io.*;
import java.net.*;
import java.util.*;

public class KinectUDPClientThread extends Thread {
 
    protected DatagramSocket socket = null;
    protected boolean isopen = false;
    protected KinectClientListener listener = null;
  
    public KinectUDPClientThread(KinectClientListener _listener, int _port) throws IOException {
    	super("KinectClientThread");
        socket = new DatagramSocket(_port);
        listener = _listener;
    }
 
    public void close(){
    	isopen = false;
    	if(this.isInterrupted())
    		socket.close();
    }
    
    public void run() {
    	isopen = true;
    	int linecounter = 0;
        while (isopen) {
            try {
                byte[] buf = new byte[640];
                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
  
                InetAddress address = packet.getAddress();
                if(packet.getData()[0] == '@' && packet.getData()[1] == '%'){
                    listener.udpevent("previsously received " + linecounter + " lines.");
                    listener.udpevent("received header with " + packet.getLength() + " bytes.");
                    linecounter = 0;
                }else{
                	linecounter++;
                }
                //int port = packet.getPort();
                //packet.getLength();
                
                //String data = ">" + packet.getData()[320];
                //listener.udpevent("received: " + data);
                
            } catch (IOException e) {
                //listener.udpevent("Socket: " + e.getMessage());
                isopen = false;
            }
        }
        socket.close();
    }
    
    public void delete(){
    	this.interrupt();
    	close();
    }
     
 }