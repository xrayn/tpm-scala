package net.ra23.batman.communication

import scala.actors.Actor
import scala.actors.Actor._
import java.util.Date;
import net.ra23.tpm.debugger._;
import net.ra23.batman.messages.types._;
import net.ra23.tpm._;
import net.ra23.batman.messages.types.MessageFactory;
import net.ra23.tpm.config._;
import net.ra23.batman.messages._;
import net.ra23.batman.measurement._;
import net.ra23.batman.encyrption._;
import net.ra23.batman._;

object MsgDispatcher extends Actor {
  /**
   * dispatch the message based on content
   */
  var messageHandler: BasicMessageHandler = null;
  private def handleMessage(message: BasicMessage) {
    val date = new Date();
    message match {
      case null => TPMDebugger.log(getClass().getSimpleName() + ": message was null!", "debug");
      case msg: TmcMessage => {
        // there is no follow up!
        //DeviceWriterActor !
        messageHandler = TmcMessageHandler(msg, "broadcast") //.getFollowupMessageAsClient()
      }
      case msg: TmqMessage => {
        if (msg.isFromClient) {
          DeviceWriterActor ! TmqMessageHandler(msg, "server").getFollowupMessageAsClient()
        } else if (!msg.isFromClient) {
          DeviceWriterActor ! TmqMessageHandler(msg, "client").getFollowupMessageAsServer()
        }
      }
      case msg: TmdMessage =>
        {
          if (msg.isFromClient) {
            DeviceWriterActor ! TmdMessageHandler(msg, "server").getFollowupMessageAsClient()
          } else if (!msg.isFromClient) {
            DeviceWriterActor ! TmdMessageHandler(msg, "client").getFollowupMessageAsServer()
          }
          // inject the received aes key!
          DeviceWriterActor ! "00::insert_aes_key::" + msg.mac + "::" + DiffieHellmanKeyExchange.decryptBlowfish(msg.payload, ConnectionStorage.getPeerKey(msg.mac))
        }
        if (messageHandler.isHandled == true) {

        }
    }
  }
  /**
   * act as server instance
   * verify the message and send response!
   */
  private def handleMessageFromClient(message: BasicMessage) {

  }
  /**
   * act as client (initiated first package)
   * a protocol follow up message needs to be created!
   */
  private def handleMessageFromServer(message: BasicMessage) {

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
          handleMessage(MessageFactory(msg))
        };
      case _ => TPMDebugger.log("I have no idea what I just got.")
    }

  }
  start();

}