/*
 * Copyright (c) 2013 maybites.ch
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal 
 *  in the Software without restriction, including without limitation the rights 
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 *  copies of the Software, and to permit persons to whom the Software is furnished
 *  to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

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

public class PattrStore{

	ArrayList<String> pattrClients;
	
	private String storename;
	private PattrCallback callback;
	
	private StorePublisher publisher;
	
	public PattrStore(){
		publisher = new StorePublisher();
		pattrClients = new ArrayList<String>();
	}

	public void init(PattrCallback _callback){
		callback = _callback;
		pattrClients.clear();
	}

	public void register(String _storename) throws PattrException{
		storename = _storename;
		try{
			publisher.registerStore(this, _storename);
		} catch(DynException e){
			throw new PattrException(e.getMessage());
		}
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
		if(pattrClients.contains(event) && publisher != null){
			publisher.clientEvent(event, value);
		}
		return false;
	}
		
	/**
	 * is beeing called if the mxj-wrapper is deleted
	 */
	public void notifyDeleted(){
		publisher.notifyDeleted();
		pattrClients.clear();
	}
	
	protected class StorePublisher implements DynPublisher{
		DynPublication publication;
		ArrayList<LinkCallback> connections;
		
		StorePublisher(){
			connections = new ArrayList<LinkCallback>();
		}
		
		protected void notifyDeleted(){
			if(publication != null)
				publication.recall();
			connections.clear();
		}
		
		protected void registerStore(PattrStore store, String _storename) throws DynException{
			if(publication != null){
				publication.recall();
			}
			publication = PattrSystem.getEnv().registerStore(this, _storename);
			publication.publish();
//			Debugger.verbose("PattrStore", "Published Object: " + _storename);
		}

		protected boolean clientEvent(String event, float value){
			for(int i = 0; i < connections.size(); i++){
				connections.get(i).set(event, value);
			}
			return true;
		}
		
		public void subscriptionConnected(String distributor, DynSubscription subscription) {
			LinkCallback link = (LinkCallback) subscription.getCallbackObject();
			Debugger.verbose("PattrStore", storename + " connected to subscription '"+link.getAddress()+"'");
			connections.add(link);
		}

		public void subscriptionDisconnected(String distributor, DynSubscription subscription) {
			LinkCallback link = (LinkCallback) subscription.getCallbackObject();
			Debugger.verbose("PattrStore", storename + " disconnected from subscription '"+link.getAddress()+"'");
			connections.remove(link);
		}

		public boolean subscriptionCallback(String distributor, DynSubscription subscription) {
			LinkCallback conn = (LinkCallback) subscription.getCallbackObject();
			callback.setAddressValue(conn.getAddress(), conn.get());
			Debugger.verbose("PattrStore", storename + " subscriptionCallback: address:" + conn.getAddress() + " value:" +conn.get());
			return true;
		}
	}

}
