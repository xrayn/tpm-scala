package net.ra23.batman.communication

import scala.actors.Actor
import scala.actors.Actor._
import java.io._;
import net.ra23.tpm.debugger._;

object DeviceReaderActor extends Actor {
  val file = "/tmp/testfifo"
  //val file = "/dev/mcom"
  val test = new Array[Byte](1000)

  val device = new RandomAccessFile(file, "rw");
  def read(): String = {
    val len = device.read(test)
    val tmp = new Array[Byte](len)
    test.copyToArray(tmp)
    new String(tmp);
  }
  def act = loop {
    TPMDebugger.log("Starting char device reader");
    loop {
      MsgDispatcher ! read()
    }
  }
  start()
}