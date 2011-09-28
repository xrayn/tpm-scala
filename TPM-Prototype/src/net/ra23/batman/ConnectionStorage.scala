package net.ra23.batman

import scala.collection.mutable.Map;
import net.ra23.batman.messages.types._;
import net.ra23.tpm.debugger._;

object ConnectionStorage {
  val db = Map.empty[String, Map[String, BasicMessage]]

  val state1 = Map.empty[String, BasicMessage]
  val state2 = Map.empty[String, BasicMessage]
  val state3 = Map.empty[String, BasicMessage]
  db += ("state3" -> state3)
  db += ("state2" -> state2)
  db += ("state1" -> state1)
  val keyDb = Map.empty[String, String]
  
  def update(mac: String, state: BasicMessage): Boolean =  {
    var result = false;
    state match {
      case msg: TmcMessage if (!inDatabase(db("state1"), mac) && !inDatabase(db("state2"), mac) && !inDatabase(db("state3"), mac)) => db("state1") += (mac -> msg);keyDb(mac)=msg.partialDHKey; result=true;
      case msg: TmcMessage if (!inDatabase(db("state1"), mac) && !inDatabase(db("state2"), mac) && inDatabase(db("state3"), mac)) => db("state1") += (mac -> msg); db("state3") -= (mac); keyDb(mac)=msg.partialDHKey; result=true;
      case msg: TmqMessage if (inDatabase(db("state1"), mac) && !inDatabase(db("state2"), mac) && !inDatabase(db("state3"), mac)) => db("state2") += (mac -> msg); db("state1") -= (mac); result=true;
      case msg: TmdMessage if (!inDatabase(db("state1"), mac) && inDatabase(db("state2"), mac) && !inDatabase(db("state3"), mac)) => db("state3") += (mac -> msg); db("state2") -= (mac); result=true;
      case msg: BasicMessage => TPMDebugger.log("Dropping" + msg + " " + state + inDatabase(db("state1"), mac).toString() + inDatabase(db("state2"), mac).toString() + inDatabase(db("state3"), mac).toString(),"debug"); result=false;
      case _ => TPMDebugger.log("Unknown message"); result=false;
    }
    result;
  }
  override def toString() = {
    var res = "\n"
    for (stateDb <- db) {
      var counter = 1;
      res += stateDb._1 + "\n"
      res += "  +----------------------------------------------------------------------------------------------------------------+\n"
      res += "  +                                               State Table                                                      +\n"
      res += "  +----------------------------------------------------------------------------------------------------------------+\n"
      for (entry <- stateDb._2) {
        res += "  | " + counter + "\t | " + entry._1 + " | " + entry._2 + " |\n"
        counter += 1;
      }
      res += "  +----------------------------------------------------------------------------------------------------------------+\n"
      res += "\n"
    }
    res

  }
  def inDatabase(database: Map[String, BasicMessage], key: String): Boolean = {
    database.isDefinedAt(key)
  }
  def asList(state: String): List[List[Any]] = {
    var result = List(List("Line","Mac", "Message"));
    var count =1;
    for (row <- db(state)) {
       result =  List(count.toString(), row._1, row._2.toString()) :: result
       count=count+1;
    }
    result.reverse
  }
  def keyDbasList(): List[List[Any]] = {
    var result = List(List("Line","Mac", "Key"));
    var count =1;
    for (row <- keyDb) {
       result =  List(count.toString(), row._1, row._2.toString()) :: result
       count=count+1;
    }
    result.reverse
  }
}