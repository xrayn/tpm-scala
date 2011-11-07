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
      case msg: Option[Unicast] if msg.isInstanceOf[Unicast] => {
        unicast(msg.get)
      }
      case msg: List[Option[Unicast]] if msg.head==None => TPMDebugger.log(getClass().getSimpleName() + ": message was None so not sending message!", "debug");
      case msg: List[Option[Unicast]] => {
        TPMDebugger.log(getClass().getSimpleName() + ": sending ["+msg.length+"] messages", "debug")
        for (m <- msg) {
          if (m!=None)
        	  unicast(m.get)
        }
      }
      case msg: Option[Unicast] if (msg == None) => {
        TPMDebugger.log(getClass().getSimpleName() + ": message was None so not sending message!", "debug");
      }
      case msg: Unicast => {
        unicast(msg)
      }
      case msg: Broadcast => broadcast(msg.msg)
      case msg: String => writePlain(msg)
      case msg: Option[Any] => println("error: "+msg)
      case _ => println("UNKNOWN PACKET!");
    }

  }
  def apply(filenames: List[String]) = {
    files = filenames;
    TPMDebugger.log("Starting device writer @[" + files + "]");
    start()
  }
}