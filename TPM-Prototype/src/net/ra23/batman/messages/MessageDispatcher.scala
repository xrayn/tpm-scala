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
		TPMDebugger.log(net.ra23.batman.ConnectionStorage)
        react {
          case msg: String if msg.startsWith("01:") => {
            //TPMDebugger.log("State [1] => received [" + msg.length() + "]: " + msg);
            val message = new  TmcMessage(msg);
            net.ra23.batman.ConnectionStorage.update(message.mac, message);
            
          }
          case msg: String if msg.startsWith("02:") => {
            //TPMDebugger.log("State [2] => received [" + msg.length() + "]: " + msg);
            val message = new  TmqMessage(msg);
            net.ra23.batman.ConnectionStorage.update(message.mac, message);
          }
          case msg: String if msg.startsWith("03:") => {
            //TPMDebugger.log("State [3] => received [" + msg.length() + "]: " + msg);
            val message = new  TmdMessage(msg);
            net.ra23.batman.ConnectionStorage.update(message.mac, message);
          }
          case msg: String => {
            TPMDebugger.log("No protocol msg => " + msg)
          }
          case _ => TPMDebugger.log("I have no idea what I just got.")
        }
        
      }
	start();
}