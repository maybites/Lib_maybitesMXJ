import com.cycling74.max.*;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;


/**
 * 	<script>
 * 		<keyframe time="02d:01h:23:01.001">
 * 			<frame time="23:01.001">
 * 				<event value="/osc/adress 3 3.5 string" typetag="ifs"/>
 *  			<event value="first second third 1 4.5634 56" typetag="sssifs"/>
 * 			</frame>
 * 		</keyframe>
 * 	</script>
 * 
 *	Time string. 
 *		There are several ways to write the time:
 *			02d:01h:23:01.001
 *				2 	days 
 *				1 	hour
 *				23	minutes
 *				01	seconds
 *				001 milliseconds
 *
 * 			02d:01h:23m:01s
 *				2 	days 
 *				1 	hour
 *				23	minutes
 *				01	seconds
 *
 * 			23m:01s
 *				23	minutes
 *				01	seconds
 *
 * 			23:01
 *				23	minutes
 *				01	seconds
 *
 * 			23:01.050
 *				23	minutes
 *				01	seconds
 *				050 milliseconds
 *
 * 			01.050
 *				01	seconds
 *				050 milliseconds
 
 * @author maybites
 *
 */
public class XMLRecorder extends MaxObject
{
	
	static final String ELEMENT_SCRIPT = "script";
	static final String ELEMENT_KEYFRAME = "keyframe";
	static final String ELEMENT_FRAME = "frame";
	static final String ELEMENT_EVENT = "event";
	static final String ELEMENT_OSCEVENT = "oscevent";
	
	static final String ATTR_TIME = "time";
	static final String ATTR_VALUE = "value";
	static final String ATTR_TYPETAG = "typetag";
	static final String ATTR_TYPE = "type";
	static final String ATTR_TAG = "tag";
	static final String ATTR_ADDRESS = "address";
	
	long pastFrameTime = 500;
	long delayTime = 250;
	long lastFrameTime;
	long startTime;
	
	Document dasDOMObjekt;
	NodeIterator keyFrames;
	NodeIterator frames;
	
	String filepath;
	String path;
	
	boolean isPlaying;
	
	MaxClock clock;

	public XMLRecorder(Atom[] args)
	{
		if(args.length == 1){
			delayTime = args[0].toLong();
			pastFrameTime = delayTime * 2;
		}
		isPlaying = false;
		path = null;
		
		clock = new MaxClock(new Callback(this, "time"));
		
		declareAttribute("path", null, "setpath");
		declareInlets(new int[]{DataTypes.ALL});
		declareOutlets(new int[]{DataTypes.ALL, DataTypes.INT});
		post("XMLRecorder Version 005");
	}
	
	public void notifyDeleted(){
		clock.release();
	}
	
	public void setpath(String _path){
		path = _path;
	}
    
	private String createFilePath(String file){
		if(path != null)
			if(path.endsWith("/"))
				return path + file;
			else
				return path + "/" + file;
		else 
			return file;
	}
	
	public void read(Atom[] file){		
		if(file != null && file.length == 1){
			filepath = createFilePath(file[0].toString());
			File dieXMLDatei = new File(filepath);
			DocumentBuilderFactory dasDBFactoryObjekt = DocumentBuilderFactory.newInstance();
			post("Loaded successfull: "+dieXMLDatei.getAbsolutePath());
			try{
				DocumentBuilder dasDBObjekt = dasDBFactoryObjekt.newDocumentBuilder();
				dasDOMObjekt = dasDBObjekt.parse(dieXMLDatei);
			}
			catch (Exception e){
				error("XMLRecorder: DocumentBuilder Exceptions:" + e.getMessage());
				return;
			}
			dasDOMObjekt.getDocumentElement().normalize();
			keyFrames = new NodeIterator(dasDOMObjekt.getElementsByTagName(ELEMENT_KEYFRAME));
			if(!keyFrames.hasCurrent()){
				error("XMLRecorder: no keyframes found in file");
				return;
			}
			fileLoaded();
			clock.unset();
		} 
	}

	private long getRunningTime(){
		return (long) clock.getTime() - startTime;
	}
	
	public void time(){
		long current = getRunningTime();	
		if(isPlaying){
			if(keyFrames.hasCurrent()){
				if(frames == null){ // the keyframe has not been triggered yet
					if(checkKeyTime(current, keyFrames.getCurrent())){
						frames = new NodeIterator(keyFrames.getCurrent().getChildNodes());
						lastFrameTime = current;
					}
				}
				if(frames != null){
					//iterate through the frames until a correct node is current
					while(frames.hasCurrent() && !frames.getCurrent().getNodeName().equals(ELEMENT_FRAME)){
						frames.iterate();
					}
					if(frames.hasCurrent()){
						if((current - lastFrameTime) > getTime(frames.getCurrent())){
							// send the events out
							processEvents(new NodeIterator(frames.getCurrent().getChildNodes()));
							frames.iterate(); // and get the next frame
							lastFrameTime = current;
						}
					} else { // if there are no more frames
						frames = null; 
						keyFrames.iterate(); // make the next keyframe current
						post("all frames are through...next keyframe");
					}
				}
			} else { // there are no more keyframes: the script is over
				scriptDone();
			}
			if(keyFrames.hasNext()){ //check if maybe the next keyframe should kick in
				if(checkKeyTime(current, keyFrames.getNext())){ 
					// if this is the case
					frames = null; 
					keyFrames.iterate(); // make the next keyframe current
					post("next keyframe kicks in");
				}
			}
		}
		printTime(current);
		clock.delay(delayTime);
	}
	
	private void processEvents(NodeIterator events){
		while(events.hasCurrent()){
			Node event = events.getCurrent();
			if(event.getNodeName().equals(ELEMENT_EVENT)){
				processEvent(event);
			}
			events.iterate();
		}
	}
	
	private void processEvent(Node event){
		char[] typetag = null;
		String tag = null;
		if(event.hasAttributes()){
			Node typetagnode = event.getAttributes().getNamedItem(ATTR_TYPETAG);
			if(typetagnode != null)
				typetag = typetagnode.getNodeValue().toCharArray();
			Node tagnode = event.getAttributes().getNamedItem(ATTR_TAG);
			if(tagnode != null)
				tag = tagnode.getNodeValue();
		}
		String value = event.getTextContent();
		String[] list = value.split(" ");
		Atom[] atoms = new Atom[list.length];
		for(int i = 0; i < list.length; i++){
			if(typetag != null && typetag.length == list.length){
				if(typetag[i] == 's')
					atoms[i] = Atom.newAtom(list[i]);
				if(typetag[i] == 'i')
					atoms[i] = Atom.newAtom(Integer.parseInt(list[i]));
				if(typetag[i] == 'f')
					atoms[i] = Atom.newAtom(Float.parseFloat(list[i]));
			} else {
				if(list[i].matches("(\\d+)")){
					atoms[i] = Atom.newAtom(Integer.parseInt(list[i]));
				}else if(list[i].matches("(\\d+)\\.(\\d+)")){
					atoms[i] = Atom.newAtom(Float.parseFloat(list[i]));
				}else{
					atoms[i] = Atom.newAtom(list[i]);
				}
			}
		}
		if(tag != null)
			eventOut(tag, atoms);
		else 
			eventOut(atoms);
	}
	  
	public void start(Atom[] dasSkript){
		if(dasDOMObjekt != null){
			keyFrames = new NodeIterator(dasDOMObjekt.getElementsByTagName(ELEMENT_KEYFRAME));
			frames = null;
			isPlaying = true;
			clock.delay(0);
			startTime = (long)clock.getTime();
		}
	}
	
	/**
	 * checks if the current time has passed the frametime and if it is not to far
	 * in the future.
	 * 
	 * @param current
	 * @param frame
	 * @return
	 */
	private boolean checkKeyTime(long current, Node frame){
		long triggerFrameTime = getTime(frame);
		return (current - pastFrameTime < triggerFrameTime && current > triggerFrameTime)? true: false;
	}
	
	private long getTime(Node frame){
		if(frame.hasAttributes()){
			NamedNodeMap dieAttribute = frame.getAttributes();
			for (int i = 0; i < dieAttribute.getLength(); i++){
				Node einAttribut = dieAttribute.item(i);
				if (einAttribut.getNodeName().equals(ATTR_TIME)){
					return parseTimeString(einAttribut.getNodeValue());
				}
			}
		}
		return 0;
	}
	
	
	private long parseTimeString(String timestring){
		long time = 0;
		String[] timearray = timestring.split(":");
		for(int j = 0; j < timearray.length; j++){
			if(timearray[j].contains("d")){
				time += Long.parseLong(timearray[j].substring(0, timearray[j].indexOf("d"))) * 24 * 60 * 60 * 1000;
			}else if(timearray[j].contains("h")){
				time += Long.parseLong(timearray[j].substring(0, timearray[j].indexOf("h"))) * 60 * 60 * 1000;
			}else if(timearray[j].contains("m")){
				time += Long.parseLong(timearray[j].substring(0, timearray[j].indexOf("m"))) * 60 * 1000;
			}else if(timearray[j].contains("s")){
				time += Long.parseLong(timearray[j].substring(0, timearray[j].indexOf("s"))) * 1000;
			}else if(timearray[j].contains(".")){
				time += (long)(Float.parseFloat(timearray[j]) * 1000);
			}else{
				if(j < timearray.length - 1){ // it must be minutes
					time += Long.parseLong(timearray[j]) * 60 * 1000;
				} else if(j > 0 && j == timearray.length - 1){ // it must be seconds
					time += Long.parseLong(timearray[j]) * 1000;
				} else if(timearray.length == 1){ // it must be milliseconds
					time += Long.parseLong(timearray[j]);
				}
			}
		}
		return time;	
	}
	
	private void eventOut(String tag, Atom[] args){
		outletHigh(0, tag, args);
	}
	
	private void eventOut(Atom[] args){
		outletHigh(0, args);
	}
	
	private void printTime(long current){
		outletHigh(1, current);
	}
	
	private void scriptDone(){
		isPlaying = false;
		outletHigh(this.getInfoIdx(), "done");
	}
	
	private void fileLoaded(){
		outletHigh(this.getInfoIdx(), "loaded");
	}

	
	protected class NodeIterator{
		private int currentIndex;
		private NodeList list;
		
		protected NodeIterator(NodeList _list){
			list = _list;
			if(list.getLength() > 0)
				currentIndex = 0;
			else
				currentIndex = -1;
		}

		public boolean hasCurrent(){
			return (currentIndex >= 0)?true: false;
		}

		public Node getCurrent(){
			if(hasCurrent())
				return list.item(currentIndex);
			else
				return null;
		}

		public boolean hasNext(){
			return (hasCurrent() && currentIndex + 1 < list.getLength())? true: false;
		}

		public Node getNext(){
			if(hasNext())
				return list.item(currentIndex + 1);
			else
				return null;
		}
		
		public int size(){
			return list.getLength();
		}
		
		public void iterate(){
			currentIndex = (currentIndex + 1 < list.getLength())? currentIndex + 1: -1;
		}
				
	}
}





