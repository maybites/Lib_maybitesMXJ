
import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import ch.maybites.utils.Debug;
import ch.maybites.utils.DebugLogger;

public class Debugger extends MaxObject implements DebugLogger{
	
	public Debugger(Atom[] args){
		Debug.setLevelToError();
		declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL});
		this.setOutletAssist(new String[]{"info messages", "error messages"});
	}
	
	public void loadbang(){
		Debug.getInstance().setOutputObject(this);
	}

	public void debugger(String _level){
		if(_level.equals("verbose")){
			Debug.setLevelToVerbose();
		}else if(_level.equals("debug")){
			Debug.setLevelToDebug();
		}else if(_level.equals("info")){
			Debug.setLevelToInfo();
		}else if(_level.equals("warning")){
			Debug.setLevelToWarning();
		}else if(_level.equals("error")){
			Debug.setLevelToError();
		}else if(_level.equals("fatal")){
			Debug.setLevelToFatal();
		}
	}

	public void printInfo(boolean _showTime, String _level, String _className, String _message) {
		Atom[] mylist = null;
		if(_showTime){
			mylist = new Atom[]{ 
					Atom.newAtom("[" + System.currentTimeMillis() + "]"),
					Atom.newAtom(_level), 
					Atom.newAtom(_className),
					Atom.newAtom(_message)};
		} else {
			mylist = new Atom[]{ 
					Atom.newAtom(_level), 
					Atom.newAtom(_className),
					Atom.newAtom(_message)};
		}
		outlet(0, mylist);
	}

	public void printError(boolean _showTime, String _level, String _className, String _message) {
		Atom[] mylist = null;
		if(_showTime){
			mylist = new Atom[]{ 
					Atom.newAtom("[" + System.currentTimeMillis() + "]"),
					Atom.newAtom(_level), 
					Atom.newAtom(_className),
					Atom.newAtom(_message)};
		} else {
			mylist = new Atom[]{ 
					Atom.newAtom(_level), 
					Atom.newAtom(_className),
					Atom.newAtom(_message)};
		}
		outlet(1, mylist);
	}
}
