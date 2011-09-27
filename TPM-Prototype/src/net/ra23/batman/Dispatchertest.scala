package net.ra23.batman

import scala.io.Source;
import scala.actors.Actor
import scala.actors.Actor._
import java.io._;
import net.ra23.tpm.debugger._;
import net.ra23.batman.communication._;

object Dispatchertest {

  val device = DeviceReaderActor.device

  def main(args: Array[String]): Unit = {
    DeviceReaderActor.setFile(args(1));
    TPMDebugger.setFile(args(0))
    DeviceWriterActor.setFiles(args.tail.tail.toList)
    TPMDebugger.log("Start")
    TPMDebugger.log("infile loaded");
    Thread.sleep(500)
    TPMDebugger.log("Sleeping 5s");
    Thread.sleep(1000);
    DeviceReaderActor ! "START"
    try {
      while (true) {
        val command = scala.Console.readLine("Type your command:");

        command match {
          case c: String if command == "p" => {
            println("State1")
            println(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state1")))
            println("State2")
            println(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state2")))
            println("State3")
            println(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state3")))
          }
          case c: String if command.startsWith("inject") => {
            val inject = c.replace("inject ", "")
            DeviceWriterActor ! inject 
          }
          case c: String if command == "t" => {
            DeviceWriterActor ! "01::c::CLIENT_MAC::CLIENT_PARTIAL_DH_KEY"
          }
          case _ => println("error in command");
        }
        //device.write(scala.Console.readLine("Type your message:").getBytes())
        Thread.sleep(100);
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }

  }
}
