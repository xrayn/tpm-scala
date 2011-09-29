package net.ra23.batman.communication

import scala.actors.Actor
import scala.actors.Actor._
import java.io._;
import net.ra23.tpm.debugger._;

object DeviceReaderActor extends Actor {
  //val file = "/dev/mcom"
  val test = new Array[Byte](1000)
  var file = ""

  var device: File = null;
  def lockFile() {
    new File(file + ".lock").createNewFile();
    TPMDebugger.log("DeviceReader: Locking file[" + file + "]", "debug");
  }
  def unlockFile() {
    new File(file + ".lock").delete()
    TPMDebugger.log("DeviceReader: Unlocking file[" + file + "]", "debug");
  }

  def read(): String = {
    var result = ""
    val fiS = new FileInputStream(file)
    while (fiS.available() == 0) {
      Thread.sleep(10)
      
      //TPMDebugger.log("no data sleeping ... ", "debug");
    }
    val len = fiS.read(test)
    unlockFile()
    val tmp = new Array[Byte](len)
    test.copyToArray(tmp)
    result = new String(tmp);
    TPMDebugger.log("reading " + file + "[" + result + "]", "debug");
    result
  }
  def act = loop {
    TPMDebugger.log("Starting device reader @[" + file + "]");
    loop {
      MsgDispatcher ! read()
    }
  }
  def apply(filename: String) {
    file = filename;
    device = new File(file);
    //device = new RandomAccessFile(file, "rw");
    start()
  }
}