package net.ra23.batman.communication

import scala.actors.Actor
import scala.actors.Actor._

import net.ra23.tpm.debugger._;
import net.ra23.batman.messages.types._;
import net.ra23.tpm._;
import net.ra23.batman.messages.types.MessageFactory;

object MsgDispatcher extends Actor {
  private def handleMessage(message: BasicMessage) {
    message match {
      case ms: TmcMessage => {
        if (message.isFromClient) {
          DeviceWriterActor ! message.getResponseMessage()
        } else if (!message.isFromClient) {
          DeviceWriterActor ! "02::c::CLIENT_MAC::CLIENT_QUOUTE::CLIENT_SML_HASH"
        }
      }
      case ms: TmqMessage => {
        if (message.isFromClient) {
          DeviceWriterActor ! message.getResponseMessage()
        } else if (!message.isFromClient) {
          DeviceWriterActor ! "03::c::CLIENT_MAC::CLIENT_ENCRYPTION_KEY"
        }
      }
      case ms: TmdMessage => {
        if (message.isFromClient) {
          DeviceWriterActor ! message.getResponseMessage()
        }
      }
      case _ =>
        {
          TPMDebugger.log("No protocol msg => " + message)
        }
    }
  }

  def act = loop {
    //TPMDebugger.log("myActor sleeps 1000");
    //Thread.sleep(1000)
    //println(net.ra23.batman.ConnectionStorage.asList());
    TPMDebugger.log("State1", "debug")
    TPMDebugger.log(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state1")), "debug")
    TPMDebugger.log("State2", "debug")
    TPMDebugger.log(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state2")), "debug")
    TPMDebugger.log("State3", "debug")
    TPMDebugger.log(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state3")), "debug")
    TPMDebugger.log("KeyDb", "debug")
    TPMDebugger.log(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.keyDbasList()), "debug")

    react {
      case msg: String =>
        {
          val message = MessageFactory(msg);
          val isUpdated = net.ra23.batman.ConnectionStorage.update(message.mac, message)
          if (isUpdated) {
            handleMessage(MessageFactory(msg))
          }
        };
      case _ => TPMDebugger.log("I have no idea what I just got.")
    }

  }
  start();

}