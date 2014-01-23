import com.cycling74.max.*;
import com.cycling74.jitter.*;
import java.util.*;
import java.io.IOException;
import java.net.* ;

import ch.maybites.mxj.utils.kinect.*;
import ch.maybites.tools.Debugger;
import ch.maybites.tools.math.la.*;
import ch.maybites.tools.threedee.*;
/**
 * @author Martin Fršhlich
 *
 * Max object container for test purposes
 */

public class KinectClient extends MaxObject implements KinectClientListener{

	KinectTCPClientThread connection;

	public KinectClient(Atom args[]){
		declareInlets(new int[]{ DataTypes.ALL, DataTypes.ALL});
		declareOutlets(new int[]{ DataTypes.ALL, DataTypes.ALL});
		createInfoOutlet(false);
	}

	/**
	 * Open with an remote IP-address and a remote Port
	 * @param args
	 */
	public void open(Atom[] args){
		if(args.length == 2){
			if(connection == null){
				try {
					connection = new KinectTCPClientThread(this, args[0].getString(), args[1].getInt());
					connection.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void close(){
		if(connection != null){
			if(connection.isAlive()){
				post("KinectClient attempts to closing connection...");
				connection.close();
			}
			connection = null;
		}
	}
	
	public void notifyDeleted(){
		close();
	}

	public void udpevent(String info) {
		post(info);
	}

	public void tcpevent(String info) {
		post(info);
	}

	public void tcperror(String error) {
		error(error);
	}
}
