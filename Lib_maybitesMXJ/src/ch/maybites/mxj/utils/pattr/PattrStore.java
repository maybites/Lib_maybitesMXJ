package ch.maybites.mxj.utils.pattr;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import ch.maybites.tools.Debugger;
import ch.maybites.utils.dyndist.DynException;
import ch.maybites.utils.dyndist.DynPublication;
import ch.maybites.utils.dyndist.DynPublisher;
import ch.maybites.utils.dyndist.DynSubscription;

import com.cycling74.max.Callback;

public class PattrStore implements DynPublisher{

	ArrayList<String> pattrClients;
	ArrayList<LinkCallback> connections;
	
	private String storename;
	private PattrCallback callback;
	
	public PattrStore(String _storename){
		connections = new ArrayList<LinkCallback>();
		pattrClients = new ArrayList<String>();
		storename = _storename;
	}
	
	public void register() throws PattrException{
		try{
			PattrHub.getEnv().registerStore(this, storename).publish();
		} catch(DynException e){
			throw new PattrException(e.getMessage());
		}
	}
	
	public void init(PattrCallback _callback){
		callback = _callback;
		pattrClients.clear();
	}
	
	public String getStoreName(){
		return storename;
	}
	
	public ArrayList<String> getClients(){
		return pattrClients;
	}
	
	public void addClient(String clientAddress){
		pattrClients.add(clientAddress);
	}
	
	public boolean clientEvent(String event, float value){
		if(pattrClients.contains(event)){
			//Debugger.getInstance().debugMessage(this.getClass(), "connections listening: " + connections.size());
			for(int i = 0; i < connections.size(); i++){
				//Debugger.getInstance().debugMessage(this.getClass(), "connect eventmessage '" + event + "' with value '" + value + "'");
				connections.get(i).set(event, value);
			}
			return true;
		}
		return false;
	}
		
	/**
	 * is beeing called if the mxj-wrapper is deleted
	 */
	public void notifyDeleted(){
		PattrHub.getEnv().removeStore(this);
		connections.clear();
	}


	public void subscriptionConnected(String d, DynSubscription c) {
		connections.add((LinkCallback) c.getCallbackObject());
	}


	public void subscriptionDisconnected(String d, DynSubscription c) {
		connections.remove((LinkCallback) c.getCallbackObject());
	}


	public boolean subscriptionCallback(String d, DynSubscription linker) {
		LinkCallback conn = (LinkCallback) linker.getCallbackObject();
		callback.setAddressValue(conn.getAddress(), conn.get());
		return true;
	}

}
