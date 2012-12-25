import com.cycling74.max.*;
import com.cycling74.jitter.*;
import java.util.*;

import ch.maybites.mxj.utils.pattr.PattrHub;
import ch.maybites.mxj.utils.pattr.PattrCallback;
import ch.maybites.mxj.utils.pattr.PattrException;
import ch.maybites.mxj.utils.pattr.PattrStore;
import ch.maybites.tools.Debugger;
import ch.maybites.tools.math.la.*;
import ch.maybites.tools.threedee.*;
/**
 * @author Martin Fr�hlich
 *
 * Max object container for test purposes
 */
public class JPattrStorage extends MaxObject implements PattrCallback{

	private PattrStore storage;

	private final int PATTR_INLET = 1;
	private final int PATTR_OUTLET = 1;

	public JPattrStorage(Atom args[]){
		if(args.length < 1){
			this.bail("requires address");
		}
		storage = new PattrStore(args[0].toString());
		try{
			storage.register();
		}catch(PattrException e){
			bail(e.getMessage());
		}

		declareInlets(new int[]{ DataTypes.ALL, DataTypes.ALL});
		declareOutlets(new int[]{ DataTypes.ALL, DataTypes.ALL});
		createInfoOutlet(false);
	}

	public void notifyDeleted(){
		storage.notifyDeleted();
	}

	public void bang(){
		if(getInlet() == 0){
			Object[] dummy = new Object[2];// {new String("clientname"), new Float(0.0f)};
			storage.init(this);
			// sets the pattrStorage to send all changes of the clients
			outlet(PATTR_OUTLET, "outputmode", Atom.newAtom(1)); 
			// lets the pattrStorage send all the clients names
			outlet(PATTR_OUTLET, "getclientlist"); 
		}
	}

	public void setAddressValue(String address, float value){
		this.outlet(PATTR_OUTLET, new Atom[]{Atom.newAtom(address), Atom.newAtom(value)});
		//this.outlet(PATTR_OUTLET, new Atom[]{Atom.newAtom((String)argObjectArray[0]), Atom.newAtom((Float)argObjectArray[1])});
	}


	public void clientlist(Atom[] args){
		if(getInlet() == PATTR_INLET){
			if(args.length == 1){
				if(args[0].toString().equals("done"))
					notifylisteners();
				else
					storage.addClient(args[0].toString());
			}
		}
	}

	public void anything(String message, Atom[] args){
		if(getInlet() == PATTR_INLET){
			if(args.length == 1){
				if(!storage.clientEvent(message, args[0].toFloat())){
					// it must be another message...
				}
			} else {
				message(message, args);
			}
		}
	}

	private void message(String message, Atom[] args){
		post("I received a '" + message + "' message.");
		if (args.length > 0)
		{
			post("It has the following arguments: [" + args.length + "]" );
			for (int i=0;i<args.length;i++){
				if(args[i].isFloat())
					post("Float: " + args[i].toFloat());
				if(args[i].isInt())
					post("Int: " + args[i].toInt());
				if(args[i].isFloat())
					post("String: " + args[i].toString());
			}
		}

	}

	private void notifylisteners(){
		ArrayList<String> clients = storage.getClients();
		for(int i = 0; i < clients.size(); i++){
			post("registered: " + clients.get(i));
		}
	}

}
