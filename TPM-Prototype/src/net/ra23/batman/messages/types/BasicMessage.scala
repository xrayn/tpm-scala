package net.ra23.batman.messages.types

abstract class BasicMessage(msg: String) {
  val content = msg.split("::");
  val state = content(0);
  val mac = content(2);
  val typ = content(1)
  var payload = if (content(3).endsWith("\n")) content(3).substring(0, content(3).length() - 1) else content(3)
  var isFromClient = false;
  if (typ == "c") {
    isFromClient = true;
  }

  override def toString() = {
    //""+getClass().getSimpleName()+ " {state:" +state+", mac: "+ mac+", payload: "+payload+"}"
    "" + getClass().getSimpleName() + " {payload: " + payload + "}"
  }
}