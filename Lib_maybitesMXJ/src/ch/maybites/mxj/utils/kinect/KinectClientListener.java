package ch.maybites.mxj.utils.kinect;

public interface KinectClientListener {

	public void udpevent(String info);
	public void tcpevent(String info);
	public void tcperror(String error);
}
