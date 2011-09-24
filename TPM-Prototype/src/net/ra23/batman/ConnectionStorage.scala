package net.ra23.batman

import scala.collection.mutable.Map;
import net.ra23.batman.messagetypes._;
import net.ra23.tpm.debugger._;

object ConnectionStorage {
  var db = Map.empty[String, BasicMessage]

  def insert(mac: String, state: BasicMessage) {
    for (entry <- db) {
      if (entry._1 == mac) {
        return
      }
    }
    db += (mac -> state)
  }
  def update(mac: String, state: BasicMessage) {
    for (entry <- db) {
      if (entry._1 == mac) {
        entry._2 match {
          case msg: TmcMessage if (state.isInstanceOf[TmqMessage]) => db(entry._1) = state;
          case msg: TmqMessage if (state.isInstanceOf[TmdMessage]) => db(entry._1) = state;
          case msg: BasicMessage => TPMDebugger.log("Dropping" + msg + " " + state);
          case _ => TPMDebugger.log("Unknown message");
        }
      }
    }
  }
  override def toString() = {
    var counter=1;
    var res = ""
    res += "+----------------------------------------------------------------------------------------------------------------+\n"
    res += "  +                                               State Table                                                      +\n"
    res += "  +----------------------------------------------------------------------------------------------------------------+\n"
    for (entry <- db) {
      res += "  | "+counter+"\t | " + entry._1 + " | " + entry._2 + " |\n"
      counter+=1;
    }
    res += "  +----------------------------------------------------------------------------------------------------------------+\n"
    res
  }

}