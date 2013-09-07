autowatch = 1;

var nodeName;

// patcher arguments
if (jsarguments.length == 1){
    nodeName = jsarguments[0];
}

var outerNodeName = 0;
var outerNodeOutlet = 0;
var outerNodeType = 0;

var myBoxRect;
var prev = 0;

var connectionObject = new Array();
var connectionOutlet = new Array();

var prev;
var owner = this.patcher.box;
while (owner) {
	prev = owner;
 	owner = owner.patcher.box;
}

// set up inlets/outlets/assist strings
inlets = 1;
outlets = 0;

function init(){
	//post("init\n");
	var owner = this.patcher.box;
	while (owner) {
    	prev = owner;
    	owner = owner.patcher.box;
	}
	if (prev){
		//post("my owner's name is",prev.patcher.name + "\n");
		//post("my patcher's name is",this.patcher.name + "\n");
		//post("my patcher's box name is",this.patcher.box.varname + "\n");
		//post("my patcher's box position is",this.patcher.box.rect + "\n");
		//myBoxRect = this.patcher.box.rect;
	}
}

function outlet(n, i, t){
	outerNodeName = n;
 	outerNodeOutlet = i;
	outerNodeType = t;
}

function drag(diffX, diffY){
	if(this.patcher.box != null){
		var myBoxRect = this.patcher.box.rect;
		var diffX = arguments[0];
		var diffY = arguments[1];
		myBoxRect[0] += diffX;
		myBoxRect[2] += diffX;
		myBoxRect[1] += diffY;
		myBoxRect[3] += diffY;
		this.patcher.box.rect = myBoxRect;
	}
}	

function connect2inlet(i, t){
	if(t == outerNodeType){
		if(outerNodeName != 0){
			if (prev){
				if(connectionObject[i] != 0){
					//post("delete connection to: " + connectionObject.varname + " objects\n");
					prev.patcher.disconnect(connectionObject[i], connectionOutlet[i], this.patcher.box, i);
				}
				//post("looking in my owner (" + prev.patcher.name + ") for object " + messagename + "\n");
			
				var firstObject = prev.patcher.firstobject;
				while(firstObject.varname != outerNodeName){
					firstObject = firstObject.nextobject;
				}
				//post("found: " + firstObject.varname + " objects\n");
				connectionObject[i] = firstObject;
				connectionOutlet[i] = outerNodeOutlet;
				prev.patcher.connect(connectionObject[i], connectionOutlet[i], this.patcher.box, i);
				//post("connectionOutlet: " + connectionOutlet + " connectionInlet: " + connectionInlet + " \n");
			}
		}
	}
}
			
