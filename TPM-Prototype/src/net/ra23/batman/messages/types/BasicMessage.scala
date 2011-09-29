package net.ra23.batman.messages.types
import scala.collection.mutable.Map;

import java.util.{ Date, Locale }
import java.text.DateFormat
import java.text.DateFormat._
import net.ra23.tpm.config;
import net.ra23.batman.communication._;

abstract class BasicMessage(msg: String) {
  val fields = Map[String, Any]("state" -> "", "type" -> "", "mac" -> "")
  val content = msg.split("::");
  val state = content(0);
  val mac = content(2);
  val typ = content(1)
  val now = new Date
  val df = getDateInstance(LONG, Locale.GERMANY)
  var payload = if (content(3).endsWith("\n")) content(3).substring(0, content(3).length() - 1) else content(3)
  fields("date") = now.getTime() 
  fields("state") = content(0);
  fields("type") = content(1);
  fields("mac") = content(2);

  var isFromClient = false;
  if (typ == "c") {
    isFromClient = true;
  }

  override def toString() = {
    //""+getClass().getSimpleName()+ " {state:" +state+", mac: "+ mac+", payload: "+payload+"}"
    var result = "" + getClass().getSimpleName() + "{ "
    for (field <- fields) {
      result = " "+ result + field._1 + ": " + field._2.toString() + ", ";
    }
    result = result.substring(0, result.length()-2) + " }"
    result
  }
  def getResponseMessage(): Unicast
}