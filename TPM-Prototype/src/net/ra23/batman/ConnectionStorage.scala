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

  def update(mac: String, state: BasicMessage): Boolean = {
    var result = false;
    state match {
      // is not in any db
      case msg: TmcMessage if (!inDatabase(db("state1"), mac) && !inDatabase(db("state2"), mac) && !inDatabase(db("state3"), mac)) => db("state1") += (mac -> msg); keyDb(mac) = msg.partialDHKey; result = true;
      case msg: TmcMessage if (inDatabase(db("state1"), mac) && !inDatabase(db("state2"), mac) && !inDatabase(db("state3"), mac)) => {
        db("state1")(mac) = msg;
        if (keyDb(mac) != msg.partialDHKey) {
          keyDb(mac) = msg.partialDHKey;
        }
        result = true;
      } // here i do an update see action matrix      
      case msg: TmcMessage if (!inDatabase(db("state1"), mac) && !inDatabase(db("state2"), mac) && inDatabase(db("state3"), mac)) => {
        // do this only if partial DH Key changed
        if (keyDb(mac) != msg.partialDHKey) {
          db("state1") += (mac -> msg);
          db("state3") -= (mac);
          keyDb(mac) = msg.partialDHKey;
          result = true;
        } else {
          TPMDebugger.log(getClass().getSimpleName() + ": Ignoring message, DH not changed!", "debug");
          result = false;
        }
      }
      case msg: TmqMessage if (inDatabase(db("state1"), mac) && !inDatabase(db("state2"), mac) && !inDatabase(db("state3"), mac)) => db("state2") += (mac -> msg); db("state1") -= (mac); result = true;
      case msg: TmqMessage if (!inDatabase(db("state1"), mac) && inDatabase(db("state2"), mac) && !inDatabase(db("state3"), mac)) => db("state2") -= (mac); db("state2") += (mac -> msg); result = true; //refresh
      case msg: TmdMessage if (!inDatabase(db("state1"), mac) && inDatabase(db("state2"), mac) && !inDatabase(db("state3"), mac)) => db("state3") += (mac -> msg); db("state2") -= (mac); result = true;
      case msg: BasicMessage => TPMDebugger.log("Dropping" + msg + " " + state + inDatabase(db("state1"), mac).toString() + inDatabase(db("state2"), mac).toString() + inDatabase(db("state3"), mac).toString(), "debug"); result = false;
      case _ => TPMDebugger.log("Unknown message"); result = false;
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
    var result = List(List("Line", "Mac", "Message"));
    var count = 1;
    for (row <- db(state)) {
      result = List(count.toString(), row._1, row._2.toString()) :: result
      count = count + 1;
    }
    result.reverse
  }
  def keyDbasList(): List[List[Any]] = {
    var result = List(List("Line", "Mac", "DH Public Key"));
    var count = 1;
    for (row <- keyDb) {
      result = List(count.toString(), row._1, row._2.toString()) :: result
      count = count + 1;
    }
    result.reverse
  }
  def getPeerKey(mac: String): Option[String] = {
    keyDb.get(mac);
  }
  def isInState3(mac: String): Boolean  = {
    state3.isDefinedAt(mac);
  }
  def getNotInState3(): List[String] = {
    var result= List[String]() 
    for ((mac,key) <- keyDb) {
      if (!isInState3(mac)) result= mac::result
    }
    result
  }
  def getDhKey(mac: String): Option[String] = {
    keyDb.get(mac)
  }
}