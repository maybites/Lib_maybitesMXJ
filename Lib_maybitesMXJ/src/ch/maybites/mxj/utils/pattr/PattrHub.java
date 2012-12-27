package ch.maybites.mxj.utils.pattr;

import java.util.*;

import ch.maybites.tools.Debugger;
import ch.maybites.utils.dyndist.DynDistributor;
import ch.maybites.utils.dyndist.DynPublication;
import ch.maybites.utils.dyndist.DynPublisher;
import ch.maybites.utils.dyndist.DynSubscriber;
import ch.maybites.utils.dyndist.DynSubscription;

public class PattrHub{
		
	private static PattrHub theonlyone = new PattrHub();

	private DynDistributor<Object, LinkCallback> factory;
	
	private PattrHub(){
		factory = new DynDistributor<Object, LinkCallback>("PattrHub");
	}
	
	public static PattrHub getEnv(){
		return theonlyone;
	}

	/**
	 * 
	 * @param registrar
	 * @param storename the pattr storage address
	 * @param address the parameter address
	 * @param listener the listeners instance
	 * @param getMethod the getter method which has to return a float
	 * @param setMethod the setter method which has to expect a float
	 * @return
	 * @throws PattrException if the get or set-methods dont exist
	 */
	public DynSubscription createLink(
			DynSubscriber registrar,
			String storename, 
			String address, 
			Object listener, 
			String getMethod, 
			String setMethod) throws PattrException{

		return factory.create(registrar, storename, new LinkCallback(address, listener, getMethod, setMethod));
	}
	
	public void removeLinks(DynSubscriber registrar){
		factory.unsubscribe(registrar);
	}
	
	protected DynPublication registerStore(PattrStore store, String storename){
		return factory.create(store, storename, null);
	}
	
	protected void removeStore(PattrStore store){
		factory.recall(store.getStoreName());
	}


		
}
