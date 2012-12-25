package ch.maybites.mxj.utils.pattr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LinkCallback {

	private Method getter;
	private Method setter;

	private Object client;
	private String address;
	
	public LinkCallback(String _address, Object _client, String _getter, String _setter) throws PattrException{
		client = _client;
		address = _address;
		try{
			if(_getter != null)
				getter = _client.getClass().getDeclaredMethod(_getter, new Class[] {});
			if(_setter != null)
				setter = _client.getClass().getDeclaredMethod(_setter, new Class[] {Float.TYPE});	
		} catch (Exception e){
			throw new PattrException("Check if you listener method has the specifies getter ["+_getter+"] and setter ["+_setter+"] methods.");
		}
	}
	
	public String getAddress(){
		return address;
	}
	
	protected void set(String _address, float _value){
		if(_address.equals(address) && setter != null){
			try {
				setter.invoke(client, _value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected float get(){
		float ret = 0.0f;
		if(getter != null){
			try {
				ret = ((Float)getter.invoke(client, null)).floatValue();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

}
