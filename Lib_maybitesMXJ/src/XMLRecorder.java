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
	
	static final String ATTR_TIME = "time";
	static final String ATTR_VALUE = "value";
	static final String ATTR_TYPETAG = "typetag";
	
	long naechsterZeitpunkt = 0;
	int derEventCounter;
	Document dasDOMObjekt;
	NodeIterator keyFrames;
	NodeIterator frames;
	
	String filepath;
	
	long keyframetime;

	public XMLRecorder(Atom[] args)
	{
		declareInlets(new int[]{DataTypes.INT, DataTypes.LIST});
		declareOutlets(new int[]{DataTypes.ALL,DataTypes.ALL});
		post("XMLRecorder Version 003");
	}
    
	public void read(Atom[] path){		
		if(path != null && path.length == 1){
			filepath = path[0].toString();
			derEventCounter = 0;
			naechsterZeitpunkt = 0;
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
			post("Found "+keyFrames.size()+" keyframes.");
		} 
	}

	public void time(int aktuelleZeit){
		if(keyFrames.hasCurrent()){
			if(frames == null){ // the keyframe has not been triggered yet
				if(aktuelleZeit > getTime(keyFrames.getCurrent())){
					frames = new NodeIterator(keyFrames.getCurrent().getChildNodes());
					keyframetime = aktuelleZeit;
				}
			}
			if(frames != null){
				//iterate through the frames until a correct node is current
				while(frames.hasCurrent() && !frames.getCurrent().getNodeName().equals(ELEMENT_FRAME)){
					frames.iterate();
				}
				if(frames.hasCurrent()){
					int nodetime = getTime(frames.getCurrent());
					if((aktuelleZeit - keyframetime) > getTime(frames.getCurrent())){
						// send the events out
						processEvents(new NodeIterator(frames.getCurrent().getChildNodes()));
						frames.iterate(); // and get the next frame
					}
				} else { // if there are no more frames
					frames = null; 
					keyFrames.iterate(); // make the next keyframe current
				}
			}
		}
		if(keyFrames.hasNext()){ //check if maybe the next keyframe should kick in
			if(aktuelleZeit > getTime(keyFrames.getNext())){ 
				// if this is the case
				frames = null; 
				keyFrames.iterate(); // make the next keyframe current
			}
		}
	}
	
	public void processEvents(NodeIterator events){
		while(events.hasCurrent()){
			Node event = events.getCurrent();
			if(event.getNodeName().equals(ELEMENT_EVENT)){
				post("value: " + event.getTextContent());
			}
			events.iterate();
		}
	}
	    
	public void play(Atom[] dasSkript){
		if(dasDOMObjekt != null){
			keyFrames = new NodeIterator(dasDOMObjekt.getElementsByTagName(ELEMENT_KEYFRAME));
			frames = null;
		}
	}
	
	private int getTime(Node frame){
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
	
	
	private int parseTimeString(String timestring){
		int time = 0;
		String[] timearray = timestring.split(":");
		for(int j = 0; j < timearray.length; j++){
			if(timearray[j].contains("d")){
				time += Integer.parseInt(timearray[j].substring(0, timearray[j].indexOf("d"))) * 24 * 60 * 60 * 1000;
			}else if(timearray[j].contains("h")){
				time += Integer.parseInt(timearray[j].substring(0, timearray[j].indexOf("h"))) * 60 * 60 * 1000;
			}else if(timearray[j].contains("m")){
				time += Integer.parseInt(timearray[j].substring(0, timearray[j].indexOf("m"))) * 60 * 1000;
			}else if(timearray[j].contains("s")){
				time += Integer.parseInt(timearray[j].substring(0, timearray[j].indexOf("s"))) * 1000;
			}else if(timearray[j].contains(".")){
				time += (int)(Float.parseFloat(timearray[j]) * 1000f);
			}else{
				if(j < timearray.length - 1){ // it must be minutes
					time += Integer.parseInt(timearray[j]) * 60 * 1000;
				} else if(j > 0 && j == timearray.length - 1){ // it must be seconds
					time += Integer.parseInt(timearray[j]) * 1000;
				} else if(timearray.length == 1){ // it must be milliseconds
					time += Integer.parseInt(timearray[j]);
				}
			}
		}
		return time;	
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
			return (currentIndex + 1 < list.getLength())? true: false;
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





