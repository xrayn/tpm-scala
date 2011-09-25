package net.ra23.batman.messages.types

abstract class BasicMessage(msg: String) {
	val content = msg.split("::");
	val state = content(0);
	val mac = content(1);
	var payload= if(content(2).endsWith("\n"))  content(2).substring(0, content(2).length()-1) else content(2) 
	
	override def toString() = {
	  //""+getClass().getSimpleName()+ " {state:" +state+", mac: "+ mac+", payload: "+payload+"}"
	  ""+getClass().getSimpleName()+ " {payload: "+payload+"}"
	}
}