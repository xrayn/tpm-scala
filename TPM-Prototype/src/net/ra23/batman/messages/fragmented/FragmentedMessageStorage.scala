package net.ra23.batman.messages.fragmented

import scala.collection.mutable.Map
import net.ra23.helper.PayloadHelper;
import net.ra23.tpm.debugger.TPMDebugger;
import scala.util.matching.Regex;

object FragmentedMessageStorage {
  var storage = Map[String, List[FragmentedMessage]]()

  def insertAndMerge(msg: String): Option[String] = {
    val fragmented = FragmentedMessage(msg)
    var result: Option[String] = None
    insert(fragmented)
    if (isComplete(fragmented)) {
      result = Some(merge(fragmented))
    }
    result
  }
  private def insert(msg: FragmentedMessage): Boolean = {
    TPMDebugger.log(getClass().getSimpleName() + ":insert ["+msg+"]", "debug");
    
    if (storage.isDefinedAt(msg.packet_meta)) {
      storage(msg.packet_meta) = msg :: storage(msg.packet_meta)
    } else {
      storage(msg.packet_meta) = List(msg)
    }
    true
  }
  private def isComplete(msg: FragmentedMessage): Boolean = {
    
    var result = false;
    if (storage.isDefinedAt(msg.packet_meta)) {
      if (storage(msg.packet_meta).length == msg.packet_max.toInt) {
        result = true
      } else {
        TPMDebugger.log(getClass().getSimpleName() + ":storage_len["+storage(msg.packet_meta).length+"]", "debug");
        TPMDebugger.log(getClass().getSimpleName() + ":message_max["+msg.packet_max+"]", "debug");
        TPMDebugger.log(getClass().getSimpleName() + ":storage ["+storage+"]", "debug");
      }
    }
    TPMDebugger.log(getClass().getSimpleName() + ":isComplete ["+result+"]", "debug");
    result
  }
  private def merge(msg: FragmentedMessage): String = {
    
    val result = msg.state+"::"+msg.typ+"::"+msg.mac+"::"+PayloadHelper.mergePayload(for (message <- storage(msg.packet_meta).reverse) yield message.payload)
    TPMDebugger.log(getClass().getSimpleName() + ":merged -> ["+result+"]", "debug");
    storage.removeKey(msg.packet_meta)
    result
  }
  def isFragmentedMessage(msg: String): Boolean = {
    val expression = """\d\df::.*""".r
    var res = false;
    if (expression.findPrefixMatchOf(msg)!=None) { 
      TPMDebugger.log(getClass().getSimpleName() + ":found fragmented message", "debug");
      res = true;
    }
    res
  }
  def cleanup(mac: String) {
    println(storage)
	 if (storage.isDefinedAt("02f::s::"+mac)){
	   storage.remove("02f::s::"+mac);
	   TPMDebugger.log(getClass().getSimpleName() + ":cleanup[02f::s::"+mac+"] succeeded", "info");
	 } else if (storage.isDefinedAt("02f::c::"+mac)){
	   storage.remove("02f::c::"+mac);
	   TPMDebugger.log(getClass().getSimpleName() + ":cleanup[02f::c::"+mac+"] succeeded", "info");
	 } else {
	   TPMDebugger.log(getClass().getSimpleName() + ":cleanup[02f::s::"+mac+"] did not succeeded => caution, maybe sequence number bug not handled properly", "info");
	 }
  }

}