package net.ra23.batman

import scala.io.Source;
import scala.actors.Actor
import scala.actors.Actor._
import java.io._;
import net.ra23.tpm.debugger._;
import net.ra23.batman.communication._;

object Dispatchertest {
  val file = DeviceReaderActor.file
  val device = DeviceReaderActor.device
  def main(args: Array[String]): Unit = {
    
    TPMDebugger.log("Start")
    TPMDebugger.log("infile loaded");
    Thread.sleep(500)
    TPMDebugger.log("Sleeping 5s");
    Thread.sleep(1000);
    DeviceReaderActor ! "START"
    try {
      while (true) {
        device.write(scala.Console.readLine("Type your message:").getBytes())
        Thread.sleep(100);
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }

  }
}
