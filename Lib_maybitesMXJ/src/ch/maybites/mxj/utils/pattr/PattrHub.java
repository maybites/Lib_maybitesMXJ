package ch.maybites.mxj.utils.pattr;

import java.util.*;

import ch.maybites.tools.Debugger;
import ch.maybites.utils.dynlinker.DynFactory;
import ch.maybites.utils.dynlinker.DynLink;
import ch.maybites.utils.dynlinker.DynLinkRegistrar;
import ch.maybites.utils.dynlinker.DynPointer;
import ch.maybites.utils.dynlinker.DynPointerRegistrar;

public class PattrHub{
		
	private static PattrHub theonlyone = new PattrHub();

	private DynFactory<Object, LinkCallback> factory;
	
	private PattrHub(){
		factory = new DynFactory<Object, LinkCallback>();
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
	public DynLink createLink(
			DynLinkRegistrar registrar,
			String storename, 
			String address, 
			Object listener, 
			String getMethod, 
			String setMethod) throws PattrException{

		return factory.createLink(registrar, storename, new LinkCallback(address, listener, getMethod, setMethod));
	}
	
	public void removeLinks(DynLinkRegistrar registrar){
		factory.disconnectLinks(registrar);
	}
	
	protected DynPointer registerStore(PattrStore store, String storename){
		return factory.createPointer(store, storename, null);
	}
	
	protected void removeStore(PattrStore store){
		factory.unregisterPointer(store.getStoreName());
	}


		
}
