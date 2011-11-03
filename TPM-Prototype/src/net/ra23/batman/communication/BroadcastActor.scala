package net.ra23.batman.communication

import scala.actors.Actor
import scala.actors.Actor._
import java.io._;
import net.ra23.tpm.debugger._;
import java.lang.ProcessBuilder;
import net.ra23.tpm.config._;

object BroadcastActor extends Actor {
  def act = loop {
    react {
      case msg: String if msg == "start" => {
        startBroadcast
      }
      case msg: String if msg == "restart" => {
        restart
      }
      case _ => println(getClass().getSimpleName() + "error in react");
    }
  }
  def startBroadcast = {
    while (true) {
      TPMDebugger.log(getClass().getSimpleName() + ": Broadcast", "debug");
      DeviceWriterActor ! Broadcast("01::c::" + TPMConfiguration.mac + "::" + TPMConfiguration.partialDHKey.toString)
      Thread.sleep(1000);
    }
  }
  start
}