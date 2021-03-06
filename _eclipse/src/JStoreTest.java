import ch.maybites.mxj.utils.pattr.PattrException;
import ch.maybites.mxj.utils.pattr.PattrSystem;
import ch.maybites.mxj.utils.pattr.PattrStore;
import ch.maybites.utils.dyndist.DynSubscriber;
import ch.maybites.utils.dyndist.DynSubscription;

import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;


/**
 * Dysfunctional - can be removed...
 * @author maybites
 *
 */
@Deprecated
public class JStoreTest extends MaxObject implements DynSubscriber {

	String store;
	
	float value1, value2, value3, value4;
	DynSubscription val1, val2, val3, val4;
	
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
			val1 = PattrSystem.getEnv().createLink(this, store, this, "getValue1", "setValue1");
			val2 = PattrSystem.getEnv().createLink(this, store, this, "getValue2", "setValue2");
			val3 = PattrSystem.getEnv().createLink(this, store, this, "getValue3", "setValue3");
			val4 = PattrSystem.getEnv().createLink(this, store, this, "getValue4", "setValue4");
			val1.subscribe();
			val2.subscribe();
			val3.subscribe();
			val4.subscribe();
		} catch (PattrException e){
			this.bail(e.getMessage());
		}
	}

	public void notifyDeleted(){
		PattrSystem.getEnv().removeLinks(this);
	}

	public void inlet(float f){
		switch(getInlet()){
		case 1:
			setValue1(f);
			val1.callback(this);
			break;
		case 2:
			setValue2(f);
			val2.callback(this);
			break;
		case 3:
			setValue3(f);
			val3.callback(this);
			break;
		case 4:
			setValue4(f);
			val4.callback(this);
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


	public void publicationConnected(String d, DynSubscription c) {
	}

	public void publicationDisonnected(String d, DynSubscription c) {
	}
	
}
