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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ch.maybites.tools.Debugger;

public class LinkCallback {

	private Method getter;
	private Method setter;

	private Object client;
	private String address;
	
	private float value = 0;
	
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
		if(_address.equals(address) && setter != null && value != _value){
			try {
				setter.invoke(client, _value);
				value = _value;
			} catch (Exception e) {
				Debugger.error(getClass(), "Setter invoke failed. Address:"+address+" | Client:" + client.getClass().getName() + " | Method:"+setter.getName());
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
