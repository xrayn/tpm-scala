package net.ra23.batman

import scala.io.Source;

import java.net._
import scala.actors.Actor
import scala.actors.Actor._
import java.io._;
import net.ra23.tpm.debugger._;
import net.ra23.batman.communication._;
import net.ra23.tpm.config._;
import scala.sys.process.Process;
import net.ra23.batman.measurement._;
import net.ra23.batman.encyrption._;

object Dispatchertest {
	
  val device = DeviceReaderActor.device
  
  def init(args: Array[String]) = {
    val localNetworkInterface = NetworkInterface.getByName("eth0");
    val localMacAddress = localNetworkInterface.getHardwareAddress.toList.map(b => String.format("%02x",b.asInstanceOf[AnyRef])).mkString(":")
    MessageMeasurer.setFile("/tmp/measurement.log")
    MessageMeasurer.measure(null, "startup");
    TPMDebugger.setFile(args(1))
    TPMDebugger.log("Start")
    TPMConfiguration.mac = localMacAddress
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
  }
  
  def setupDh() = {
    
  }
  def main(args: Array[String]): Unit = {
    init(args)

    try {
      // start automatically to broadcast!
      //BroadcastActor ! "start"

      /*
    	 * first of all init the kernel module!
    	 */
      val random = new scala.util.Random();
     
      //           1234567890123456   <- is 16 Byte char array!
      val aesKey = scala.math.abs(random.nextLong()).toString.substring(0, 16);
      //change this later to dynamic data
      //TPMConfiguration.partialDHKey = dhKey
      TPMConfiguration.aesKey = aesKey
      println("[Init DH Partial Key ......] " + DiffieHellmanKeyExchange.getPublicKey())
      DeviceWriterActor ! "00::init_dh_key::" + DiffieHellmanKeyExchange.getPublicKey()
      println("[INIT AES Key        ......] " + TPMConfiguration.aesKey)
      DeviceWriterActor ! "00::init_aes_key::" + TPMConfiguration.aesKey;

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
              DeviceWriterActor ! Some(Unicast("02::" + mac + "::02::c::" + TPMConfiguration.mac + "::CLIENT_QUOUTE::CLIENT_SML_HASH"))
            }
          }
          case c: String if command == "b" => {
            println("[Starting to broadcast ......]")
            BroadcastActor ! "start"
          }
          case c: String if command == "c" => {

            val aesKey = scala.math.abs(random.nextLong()).toString.substring(0, 16);
            //change this later to dynamic data
            
            TPMConfiguration.aesKey = aesKey
            DiffieHellmanKeyExchange.newSecretKey();
            println("[CHANGE DH Partial Key ......] " + DiffieHellmanKeyExchange.getPublicKey())
            DeviceWriterActor ! "00::init_dh_key::" + DiffieHellmanKeyExchange.getPublicKey()
            println("[CHANGE AES Key        ......] " + TPMConfiguration.aesKey)
            DeviceWriterActor ! "00::init_aes_key::" + TPMConfiguration.aesKey;
          }
          case c: String if command == "s" => {
            val random = new scala.util.Random();
            val dhKey = scala.math.abs(random.nextLong());
            //           1234567890123456   <- is 16 Byte char array!
            val aesKey = "AES_KEY000000000" //scala.math.abs(random.nextLong()).toString.substring(0,16);
            //change this later to dynamic data
            TPMConfiguration.partialDHKey = dhKey
            TPMConfiguration.aesKey = aesKey
            println("[Changing DH Partial Key ......] " + TPMConfiguration.partialDHKey)
            DeviceWriterActor ! "00::init_dh_key::" + TPMConfiguration.partialDHKey
            println("[Changing AES Key        ......] " + TPMConfiguration.aesKey)
            DeviceWriterActor ! "00::init_aes_key::" + TPMConfiguration.aesKey;
            for ((mac, partialDhKey) <- net.ra23.batman.ConnectionStorage.keyDb) {
              DeviceWriterActor ! Some(Unicast("02::" + mac + "::02::c::" + TPMConfiguration.mac + "::CLIENT_QUOUTE::CLIENT_SML_HASH"))
            }
          }
          case c: String if command == "h" => {
            consoleHelp
          }
          case c: String if command == "q" => {
            //            println("removing ["+in+"]")
            //            val pb = Process("""rm -f """+in)
            //            pb.!
            //            println("removing ["+in+".lock]")
            //            val pb2 = Process("""rm -f """+in+".lock")
            //            pb2.!
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
    println("[c] -> change dh key + aes key")
    println("[t] -> inject a tmq package (start protocol)")
    println("[p] -> show current state tables and keydb")
    println("[s] -> inject aes + payload and start protocol with tmq")
  }
}
