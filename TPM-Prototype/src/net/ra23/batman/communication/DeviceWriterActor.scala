package net.ra23.batman.communication

import scala.actors.Actor
import scala.actors.Actor._
import java.io._;
import net.ra23.tpm.debugger._;
import scala.collection.mutable.ListBuffer

object DeviceWriterActor extends Actor {
  var files = List[String]();
  //val file = "/dev/mcom"
  var devices = ListBuffer[RandomAccessFile]()
  
  def write(value: String) = {

    for (device <- devices) {
      TPMDebugger.log("writing to " + device.toString() + "[" + value + "]", "debug");
      device.write(value.getBytes())
    }
  }
  def act = loop {

    TPMDebugger.log("Starting device writer @[" + files + "]");
    loop {
      react {
        case msg: String =>  write(msg);
        case msg: Any => println(msg);
        case _ => println("error");
      }
    }
  }
  start()

  def setFiles(filenames: List[String]) {
    files = filenames;
    for (file <- files) {
    devices.append(new  RandomAccessFile(file, "rw"))
  }
  }
}