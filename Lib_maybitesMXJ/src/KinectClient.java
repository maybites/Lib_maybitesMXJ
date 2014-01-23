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

	private String ipaddress;
	private int port;

	KinectTCPClientThread connection;

	public KinectClient(Atom args[]){
		if(args.length < 1){
			this.bail("KinectClient: requires a local port number");
		}
		port = args[0].toInt();

		declareAttribute("port", null, "setport");
		declareInlets(new int[]{ DataTypes.ALL, DataTypes.ALL});
		declareOutlets(new int[]{ DataTypes.ALL, DataTypes.ALL});
		createInfoOutlet(false);
	}
	
	public void setaddress(String _address){
		ipaddress = _address;
	}
	
	public void setport(int _port){
		port = _port;
	}
	
	/**
	 * Open with an remote IP-address and a remote Port
	 * @param args
	 */
	public void open(Atom[] args){
		if(args.length == 2){
			if(connection == null){
				try {
					connection = new KinectTCPClientThread(this, args[0].getString(), args[1].getInt(), port);
					post("KinectClient starts tcp-client on port: " + port);
					connection.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void close(){
		if(connection != null){
			post("KinectClient attempts to closing tcp-client port on: " + port);
			connection.close();
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
