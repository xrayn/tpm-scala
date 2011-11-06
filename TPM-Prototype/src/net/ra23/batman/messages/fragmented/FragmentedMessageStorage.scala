package net.ra23.batman.messages.fragmented

import scala.collection.mutable.Map
import net.ra23.helper.PayloadHelper;

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
    println("insert ---> " +msg)
    println()
    
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
        println(storage(msg.packet_meta).length)
        println(storage)
        println(msg.packet_max)
      }
    }
    println("isComplete ["+result+"]")
    result
  }
  private def merge(msg: FragmentedMessage): String = {
    
    val result = msg.state+"::"+msg.typ+"::"+msg.mac+"::"+PayloadHelper.mergePayload(for (message <- storage(msg.packet_meta).reverse) yield message.payload)
    println("merged -->["+result+"]")
    storage.removeKey(msg.packet_meta)
    result
  }
  def isFragmentedMessage(msg: String): Boolean = {
    val result = msg.startsWith("02f")
    if (result) println(msg)
    result
  }

}