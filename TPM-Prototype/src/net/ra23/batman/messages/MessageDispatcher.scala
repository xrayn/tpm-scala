package net.ra23.batman.communication

import scala.actors.Actor
import scala.actors.Actor._

import net.ra23.tpm.debugger._;
import net.ra23.batman.messages.types._;
import net.ra23.tpm._;

object MsgDispatcher extends Actor {
  def act = loop {
    //TPMDebugger.log("myActor sleeps 1000");
    //Thread.sleep(1000)
    //println(net.ra23.batman.ConnectionStorage.asList());
    TPMDebugger.log("State1","debug")
    TPMDebugger.log(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state1")),"debug")
    TPMDebugger.log("State2","debug")
    TPMDebugger.log(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state2")),"debug")
    TPMDebugger.log("State3","debug")
    TPMDebugger.log(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state3")),"debug")
    react {
      case msg: String if msg.startsWith("01:") => {
        //TPMDebugger.log("State [1] => received [" + msg.length() + "]: " + msg);
        val message = new TmcMessage(msg);
        val isUpdated= net.ra23.batman.ConnectionStorage.update(message.mac, message)
        if ( isUpdated && message.isFromClient){
          DeviceWriterActor ! message.getResponseMessage()
        } else if (isUpdated && ! message.isFromClient) {
          DeviceWriterActor ! "02::c::CLIENT_MAC::CLIENT_QUOUTE::CLIENT_SML_HASH"
        }

      }
      case msg: String if msg.startsWith("02:") => {
        //TPMDebugger.log("State [2] => received [" + msg.length() + "]: " + msg);
        val message = new TmqMessage(msg);
        val isUpdated= net.ra23.batman.ConnectionStorage.update(message.mac, message)
        if ( isUpdated && message.isFromClient){
          DeviceWriterActor ! message.getResponseMessage()
        } else if (isUpdated && ! message.isFromClient) {
          DeviceWriterActor ! "03::c::CLIENT_MAC::CLIENT_ENCRYPTION_KEY"
        }
      }
      case msg: String if msg.startsWith("03:") => {
        //TPMDebugger.log("State [3] => received [" + msg.length() + "]: " + msg);
        val message = new TmdMessage(msg);
        val isUpdated= net.ra23.batman.ConnectionStorage.update(message.mac, message)
        if ( isUpdated && message.isFromClient){
          DeviceWriterActor ! message.getResponseMessage()
        }
//        else if (isUpdated && ! message.isFromClient) {
//          DeviceWriterActor ! "01::c::FROM_CLIENT::CLIENTNONCE"
//        }
      }
      case msg: String => {
        TPMDebugger.log("No protocol msg => " + msg)
      }
      case _ => TPMDebugger.log("I have no idea what I just got.")
    }

  }
  start();
}