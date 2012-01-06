package net.ra23.batman

import scala.io.Source;
import net.ra23.tpm.base._;

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
import net.ra23.tpm.sign.TPMSigning;
import net.ra23.tpm.base._;
import net.ra23.helper.PayloadHelper;

object Dispatchertest {

  val device = DeviceReaderActor.device
  val baseDir = new File("").getAbsolutePath();
  def init(args: Array[String]) = {
    val configfile = baseDir + "/etc/" + args(0);
    TPMDebugger.log("Loading config from " + configfile);
    TPMConfiguration.fromXmlFile(configfile)
    println(TPMConfiguration);
    val localNetworkInterface = NetworkInterface.getByName(TPMConfiguration.get("networkInterface"));
    val localMacAddress = localNetworkInterface.getHardwareAddress.toList.map(b => String.format("%02x", b.asInstanceOf[AnyRef])).mkString(":")
    MessageMeasurer.setFile(TPMConfiguration.get("measurementLogFile"))
    NodesMeasurer.setFile(TPMConfiguration.get("measurementLogFile"))
    MessageMeasurer.measure(null, "startup");
    TPMDebugger.setFile(TPMConfiguration.get("debugLogFile"))
    TPMDebugger.log("Start")
    TPMConfiguration.mac = localMacAddress
    val in = TPMConfiguration.get("kernelCommDeviceReader");
    val out = List(TPMConfiguration.get("kernelCommDeviceWriter"));

    TPMDebugger.log("infile loaded");
    Thread.sleep(500)
    TPMDebugger.log("initialize tpm");
    /* deactivated until tpm device available at clients! */
    TPM.init();
    TPMSigning.selfTest()
    if (TPMConfiguration.get("measureQuote") != "0") {
      TPMSigning.measure();
    }
    DeviceWriterActor(out)

    initKernelModule()
    Thread.sleep(1000)
    DeviceReaderActor(in)
    DeviceReaderActor ! "START"

    println(TPMConfiguration.partialDHKey.toString)

  }

  def setupDh() = {

  }
  def initKernelModule(mode: String = "Init") {
    val random = new scala.util.Random();
    // should read /dev/urandom
    // then encode it to an hexString --needs further implementation in kernel module --
    // @lookhere: org.apache.commons.codec.binary.Hex
    val aesKey = scala.math.abs(random.nextLong()).toString.substring(0, 16);
    DiffieHellmanKeyExchange.newSecretKey();
    TPMConfiguration.aesKey = aesKey
    println("[" + mode + " DH Partial Key ......] " + DiffieHellmanKeyExchange.getPublicKey())
    DeviceWriterActor ! "00::init_dh_key::" + DiffieHellmanKeyExchange.getPublicKey()
    println("[" + mode + " AES Key        ......] " + TPMConfiguration.aesKey)
    DeviceWriterActor ! "00::init_aes_key::" + TPMConfiguration.aesKey;
  }
  def main(args: Array[String]): Unit = {
    init(args)
    try {
      /*
    	 * first of all init the kernel module!
    	 */

      if (TPMConfiguration.get("autoMode") == "1") {
        AutoconnectActor ! "start"
      }
      while (true) {
        consoleHelp
        val command = scala.Console.readLine("Type your command:")
        command match {
          case c: String if command == "p" => {
            printTable()
          }
          case c: String if command.startsWith("inject") => {
            val inject = c.replace("inject ", "")
            DeviceWriterActor ! inject
          }
          case c: String if command == "t" => {
            injectTmqMessage()
          }
          case c: String if command == "e" => {
            exportPublicSrk()
          }
          case c: String if command == "c" => {
            initKernelModule("Change")
          }
          case c: String if command == "connect" => {
            connect();
          }
          case c: String if command.startsWith("autoconnect") => {
            val cmd = command.replace("autoconnect", "").trim();
            println(cmd);
            AutoconnectActor ! cmd
          }

          case c: String if command == "s" => {
            printStats();
          }
          case c: String if command == "h" => {
            consoleHelp
          }
          case c: String if command == "q" => {
            println("Exiting....")
            System.exit(0);
          }
          case _ => println("error in command");
        }
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
    println("[e] -> export public SRK")
    println("[c] -> change dh key + aes key")
    println("[t] -> inject a tmq package (start protocol)")
    println("[p] -> show current state tables and keydb")
    println("[s] -> show stat summary")
    println("[q] -> exit client")
    println("[connect] -> connect all unconnected nodes")
    println("[autoconnect (start|stop|restart)] -> start auto connection actor")
  }
  def exportPublicSrk() {
    println("[Exporting public SRK ......]")
    TPMKeymanager.exportPublicKey(TPMKeymanager.getSRK(), baseDir + "/srk_keys/" + TPMConfiguration.mac + ".key")
  }
  def injectTmqMessage(): Unit = {
    println("[Testing tmq & tmd ......]")
    for ((mac, partialDhKey) <- net.ra23.batman.ConnectionStorage.keyDb) {
      var result = List[Option[Unicast]]()
      // tune 512 to a higher parameter, this is only for testing!
      for (payload <- PayloadHelper.splitPayload(TPMSigning.getQuoteBase64() + "::CLIENT_SML_HASH", TPMConfiguration.tmqSplitSize))
        result = Some(Unicast("02::" + mac + "::02f::c::" + TPMConfiguration.mac + "::" + payload)) :: result
      DeviceWriterActor ! result.reverse
      //PayloadHelper.splitPayload(TPMSigning.getQuoteBase64() + "::CLIENT_SML_HASH", 10).foreach(payload => DeviceWriterActor ! Some(Unicast("02::" + mac + "::02::c::" + TPMConfiguration.mac + "::"+payload)))
    }
  }
  def connect(): Unit = {
    var nodes = 0;
    println("Connecting");
    for (mac <- ConnectionStorage.getNotInState3()) {
      nodes = nodes + 1;
      var result = List[Option[Unicast]]()
      val key = ConnectionStorage.getDhKey(mac);
      if (key != None) {
        print(".")
        val partialDhKey = key.get;
        for (payload <- PayloadHelper.splitPayload(TPMSigning.getQuoteBase64() + "::CLIENT_SML_HASH", TPMConfiguration.tmqSplitSize))
          result = Some(Unicast("02::" + mac + "::02f::c::" + TPMConfiguration.mac + "::" + payload)) :: result
        DeviceWriterActor ! result.reverse
      }
    }
    Thread.sleep(2000);
    println("")
    printStats()
    println("")
  }
  def printStats() {
    println("Known Nodes: [" + ConnectionStorage.keyDb.size + "]");
    println("Unconnected Nodes: [" + ConnectionStorage.getNotInState3().length + "]");
    println("Connected Nodes: [" + (ConnectionStorage.keyDb.size - ConnectionStorage.getNotInState3().length) + "]");
  }
  def printTable(): Unit = {
    println("State1")
    println(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state1")))
    println("State2")
    println(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state2")))
    println("State3")
    println(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state3")))
    println("KeyDb")
    println(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.keyDbasList()))
  }
}
