package net.ra23.batman.communication

import scala.actors.Actor
import scala.actors.Actor._
import java.io._;
import net.ra23.tpm.debugger._;
import scala.collection.mutable.ListBuffer
import net.ra23.batman.messages.types._;

object DeviceWriterActor extends Actor {
  var files = List[String]();
//  def lockFile(filename: String) {
//    new File(filename + ".lock").createNewFile();
//    TPMDebugger.log("Locking file[" + filename + "]", "debug");
//  }
  def unlockFile(filename: String) {
    new File(filename + ".lock").delete()
    TPMDebugger.log("Unlocking file[" + filename + "]", "debug");
  }
  /*
   * checks if there is a file and only writes if the file exisits
   * 
   */
  def write(value: String, filename: String) = {
    if (new File(filename).exists()) {
      val myDevice = new RandomAccessFile(filename, "rw");
      while (new File(filename + ".lock").exists()) {
        Thread.sleep(500);
        TPMDebugger.log("waiting for file write access....", "debug");
      }
      // disabled for char device lockFile(filename);
      val myValue = value + "\n"
      myDevice.write(myValue.getBytes())
      myDevice.close
      TPMDebugger.log("writing to " + filename + "[" + value + "]", "debug");
    } else {
      TPMDebugger.log(getClass().getSimpleName() + ": Device " + filename + " is not ready yet ....", "debug");
    }
  }
  def broadcast(value: String) = {
    for (file <- files) {
      write(value, file)
    }
  }
  def unicast(message: Unicast) = {
    for (file <- files) {
      TPMDebugger.log(files, "debug");
      write(message.msg, file)
    }
  }
  def writePlain(message: String) = {
    TPMDebugger.log(files, "debug");
    for (file <- files) {
      write(message, file)
    }
  }
  def act = loop {
    react {
      case msg: Some[Unicast] => {
        unicast(msg.get)
      }
      case msg: Option[Unicast] if (msg == None) => {
        TPMDebugger.log(getClass().getSimpleName() + ": message was None so not sending message!", "debug");
      }
      case msg: Unicast => {
        unicast(msg)
      }
      case msg: Broadcast => broadcast(msg.msg)
      case msg: String => writePlain(msg)
      case _ => println("error");
    }

  }
  def apply(filenames: List[String]) = {
    files = filenames;
    TPMDebugger.log("Starting device writer @[" + files + "]");
    start()
  }
}