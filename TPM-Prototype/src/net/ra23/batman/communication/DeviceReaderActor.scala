package net.ra23.batman.communication

import scala.actors.Actor
import scala.actors.Actor._
import java.io._;
import net.ra23.tpm.debugger._;


object DeviceReaderActor extends Actor {
  //val file = "/dev/mcom"
  val test = new Array[Byte](1000)
  var file =""
  var device:RandomAccessFile = null;
  def read(): String = {
    val len = device.read(test)
    val tmp = new Array[Byte](len)
    test.copyToArray(tmp)
    new String(tmp);
  }
  def act = loop {
    TPMDebugger.log("Starting device reader @["+file+"]");
    loop {
      MsgDispatcher ! read()
    }
  }
  def apply(filename: String) {
    file=filename;
    device = new RandomAccessFile(file, "rw");
     start()
  }
}