import ch.maybites.mxj.utils.pattr.PattrException;
import ch.maybites.mxj.utils.pattr.PattrHub;
import ch.maybites.mxj.utils.pattr.PattrStore;
import ch.maybites.utils.dynlinker.DynLink;
import ch.maybites.utils.dynlinker.DynLinkRegistrar;

import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;


public class JStoreTest extends MaxObject implements DynLinkRegistrar {

	String store;
	
	float value1, value2, value3, value4;
	DynLink val1, val2, val3, val4;
	
	public JStoreTest(Atom args[]){
		if(args.length < 1){
			this.bail("requires address");
		}
		store = args[0].toString();

		declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
		declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
		createInfoOutlet(false);
	}
	
	public void bang(){
		try{
			val1 = PattrHub.getEnv().createLink(this, store, "radio", this, "getValue1", "setValue1");
			val2 = PattrHub.getEnv().createLink(this, store, "soap", this, "getValue2", "setValue2");
			val3 = PattrHub.getEnv().createLink(this, store, "ui::radio", this, "getValue3", "setValue3");
			val4 = PattrHub.getEnv().createLink(this, store, "ui::soap", this, "getValue4", "setValue4");
			val1.connect();
			val2.connect();
			val3.connect();
			val4.connect();
		} catch (PattrException e){
			this.bail(e.getMessage());
		}
	}

	public void notifyDeleted(){
		PattrHub.getEnv().removeLinks(this);
	}

	public void inlet(float f){
		switch(getInlet()){
		case 1:
			setValue1(f);
			val1.refresh();
			break;
		case 2:
			setValue2(f);
			val2.refresh();
			break;
		case 3:
			setValue3(f);
			val3.refresh();
			break;
		case 4:
			setValue4(f);
			val4.refresh();
			break;
		}
	}

	public float getValue1(){
		return value1;
	}

	public float getValue2(){
		return value2;
	}

	public float getValue3(){
		return value3;
	}

	public float getValue4(){
		return value4;
	}

	public void setValue1(float f){
		value1 = f;
		outlet(1, value1);
	}
	
	public void setValue2(float f){
		value2 = f;
		outlet(2, value2);
	}
	
	public void setValue3(float f){
		value3 = f;
		outlet(3, value3);
	}

	public void setValue4(float f){
		value4 = f;
		outlet(4, value4);
	}


	public void connectedToPointer(DynLink c) {
	}

	public void disconnectedFromPointer(DynLink c) {
	}
	
}
