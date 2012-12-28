package ch.maybites.mxj.utils.mirror;

import com.cycling74.max.Atom;
import com.cycling74.max.MaxObject;

/**
 * This class makes the connection between a more sophisticated Jitter Interface Object and JitterMirror.
 * 
 * @author maybites
 *
 */
public class JitterConnector {

	private MaxObject max;	
	private Atom[] response;
	private int outputInlet;
	
	public JitterConnector(){
	}
	
	protected final void init(MaxObject _max, int _outputInlet){
		outputInlet = _outputInlet;
		max = _max;
	}
	
	/**
	 * This method is called as a response from the mirrored jitter object
	 * @param message
	 * @param args
	 */
	public final void maxResponse(String message, Atom[] args){
		response = new Atom[args.length + 1];
		response[0] = Atom.newAtom(message);
		for(int i = 0; i < args.length; i++){
			response[i + 1] = args[i];
		}
	}

	public final void bang(){
		output("bang", null);
	}
			
	/**
	 * A way to get other kind of messages
	 * 
	 * Atom.toInt(send(messagename, Atom.newAtom(value)))
	 * 
	 * @param messagename
	 * @param args
	 * @return
	 */
	public final Atom[] send(String messagename, Atom[] args){
		response = null;
		output(messagename, args);
		return response;
	}
	
	public final boolean setAttr(String attrname, Atom[] args){
		output(attrname, args);
		return true;
	}

	public final Atom[] send(String messagename){
		response = null;
		output(messagename, null);
		return response;
	}

	public final Atom[] getAttr(String attrname){
		response = null;
		output(attrname, null);
		return response;
	}

	public static Atom[] dropFirstIndex(Atom[] args){
		if(args.length > 1){
			Atom[] ret = new Atom[args.length - 1];
			for(int i = 1; i < args.length; i++){
				ret[i - 1] = args[i];
			}
			return ret;
		}
		return null;
	}
	
	private final void output(String messagename, Atom[] args){
		if(args != null)
			max.outlet(outputInlet, messagename, args);
		else
			max.outlet(outputInlet, messagename);
	}
}