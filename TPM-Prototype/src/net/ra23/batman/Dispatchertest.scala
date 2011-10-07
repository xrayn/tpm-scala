package net.ra23.batman

import scala.io.Source;
import scala.actors.Actor
import scala.actors.Actor._
import java.io._;
import net.ra23.tpm.debugger._;
import net.ra23.batman.communication._;
import net.ra23.tpm.config._;
import scala.sys.process.Process

object Dispatchertest {

  val device = DeviceReaderActor.device

  def main(args: Array[String]): Unit = {
    TPMDebugger.setFile(args(1))
    TPMDebugger.log("Start")
    TPMConfiguration.mac = args(0)
    val in = args(2)
    val out = args.tail.tail.tail.toList
    DeviceReaderActor(in);
    DeviceWriterActor(out)
    TPMDebugger.log("infile loaded");
    Thread.sleep(500)
    TPMDebugger.log("Sleeping 5s");
    Thread.sleep(100);
    DeviceReaderActor ! "START"
    println(TPMConfiguration.partialDHKey.toString)

    try {
      // start automatically to broadcast!
      BroadcastActor ! "start"
      while (true) {
        consoleHelp
        val command = scala.Console.readLine("Type your command:");

        command match {
          case c: String if command == "p" => {
            println("State1")
            println(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state1")))
            println("State2")
            println(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state2")))
            println("State3")
            println(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state3")))
            println("KeyDb")
            println(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.keyDbasList()))

          }
          case c: String if command.startsWith("inject") => {
            val inject = c.replace("inject ", "")
            DeviceWriterActor ! inject
          }
          case c: String if command == "t" => {
            println("[Testing tmq & tmd ......]")
            for ((mac, partialDhKey) <- net.ra23.batman.ConnectionStorage.keyDb) {
              DeviceWriterActor ! Some(Unicast("02::c::" + TPMConfiguration.mac + "::CLIENT_QUOUTE::CLIENT_SML_HASH", mac))
            }
          }
          case c: String if command == "b" => {
            println("[Starting to broadcast ......]")
            BroadcastActor ! "start"
          }
          case c: String if command == "c" => {

            val random = new scala.util.Random();
            val value = scala.math.abs(random.nextLong());
            TPMConfiguration.partialDHKey = value
            println("[Changing DH Partial Key ......] " + value)
            //BroadcastActor ! "restart"
          }
          case c: String if command == "h" => {
            consoleHelp
          }
          case c: String if command == "q" => {
            println("removing ["+in+"]")
            val pb = Process("""rm -f """+in)
            pb.!
            println("removing ["+in+".lock]")
            val pb2 = Process("""rm -f """+in+".lock")
            pb2.!
            println("Exiting....")
            System.exit(0);
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
  def consoleHelp = {
    println("Command Overview:")
    println("[h] -> print this help")
    println("[b] -> start broadcast")
    println("[c] -> change dh key")
    println("[t] -> inject a tmq package (start protocol)")
    println("[p] -> show current state tables and keydb")
  }
}
