package ch.maybites.mxj.utils.mirror;

import com.cycling74.jitter.JitterEvent;
import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

/**
 * This class is a barebone mirror structure and should be extended by classes that whish to have 
 * the mirror functionality
 * 
 * @author maybites
 *
 */
public class JitterMirror extends MaxObject{
	
	public final JitterConnector connector;

	private final int JIT_MIRROR_INLET = 2;
	private final int JIT_MIRROR_OUTLET = 2;

	private final int JIT_MSG_INLET = 1;
	private final int JIT_MSG_OUTLET = 1;

	private final int MAIN_INLET = 0;
	private final int MAIN_OUTLET = 0;

	/**
	 * This method needs to be called by a child class
	 * @param args
	 */
	public JitterMirror(Atom args[]){
		connector = new JitterConnector();

		declareInlets(new int[]{ DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
		declareOutlets(new int[]{ DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
		setInletAssist(new String[] { "bang to initialize",  "messages to jitter object", "connected to right outlet of jitterobject"});
		setOutletAssist(new String[] { "messages from this object",  "messages from jitter object", "connected to left inlet of jitterobject"});
		createInfoOutlet(false);
	}

	/**
	 * This Method needs to be called by a child class
	 */
	public void bang(){
		if(getInlet() == MAIN_INLET){
			connector.init(this, JIT_MIRROR_OUTLET);
		}
	}

	/**
	 * The Child class can overwrite this method and decide what to do with it,
	 * since some Events can be caused by user interactions to the jitter object.
	 * 
	 * Just make sure to implement the line:
	 * 
	 * super.connector.maxResponse(message, args);
	 * 
	 * otherwise queries to the jitter object will be go into that data nirvana.
	 * 
	 * @param event
	 */
	public void jitterEvent(String message, Atom[] args){
		connector.maxResponse(message, args);
	}
	
	public void anything(String message, Atom[] args){
		if(getInlet() == JIT_MIRROR_INLET){
			//This is a response message from the mirrored jitter object
			jitterEvent(message, args);
		} else if(getInlet() == JIT_MSG_INLET){
			if(message.startsWith("@") && args.length > 0){
				//This is an attribute message to the mirrored jitter object
				connector.setAttr(message.substring(1), args);
			} else{
				Atom[] ret;
				if(args.length > 0)
					//This is a message to the mirrored jitter object
					ret = connector.send(message, args);
				else
					//This is a message to the mirrored jitter object
					ret = connector.send(message);				
				if(ret != null && ret.length > 0)
					messageOutput(ret);
			}
		} else {
			// messages meant for this max object
		}
	}
	
	/**
	 * Prints a Message to the Jitter Message Outlet
	 * @param msg
	 */
	public void messageOutput(Atom[] msg){
		outlet(JIT_MSG_OUTLET, msg);
	}

	/**
	 * Prints a Message to the Jitter Message Outlet
	 * @param msg
	 */
	public void messageOutput(String message, Atom[] args){
		outlet(JIT_MSG_OUTLET, message, args);
	}

}
