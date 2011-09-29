package net.ra23.batman.communication

import scala.actors.Actor
import scala.actors.Actor._
import java.io._;
import net.ra23.tpm.debugger._;
import scala.collection.mutable.ListBuffer
import net.ra23.batman.messages.types._;

object DeviceWriterActor extends Actor {
  var files = List[String]();
  //val file = "/dev/mcom"
  //var devices = ListBuffer[RandomAccessFile]()
  var devices = Map[String, RandomAccessFile]();
  def lockFile(filename: String) {
    new File(filename + ".lock").createNewFile();
    TPMDebugger.log("Locking file[" + filename + "]", "debug");
  }
  def unlockFile(filename: String) {
    new File(filename + ".lock").delete()
    TPMDebugger.log("Unlocking file[" + filename + "]", "debug");
  }
  def write(value: String, device: RandomAccessFile, filename: String) = {
    while (new File(filename + ".lock").exists()) {
      Thread.sleep(500);
      TPMDebugger.log("waiting for file write access....", "debug");
    }
    lockFile(filename);
    device.write(value.getBytes())
    TPMDebugger.log("writing to " + device.toString() + "[" + value + "]", "debug");
  }
  def broadcast(value: String) = {
    for (device <- devices) {
      write(value, device._2, device._1)
    }
  }
  def unicast(message: Unicast) = {
    TPMDebugger.log(files, "debug");
    for (file <- files) {
      val MAC = message.mac.toUpperCase().split("_")
      val FILE = file.toUpperCase();
      TPMDebugger.log(MAC(0) + " contains " + FILE, "debug")
      if (FILE.contains(MAC(0))) {
        write(message.msg, devices(file), file)
      }
    }
  }
  def act = loop {
    TPMDebugger.log("Starting device writer @[" + files + "]");
    loop {
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
        case msg: String => TPMDebugger.log(msg, "debug")
        case _ => println("error");
      }
    }
  }
  def apply(filenames: List[String]) = {
    files = filenames;
    for (file <- files) {
      devices += (file -> new RandomAccessFile(file, "rw"))
    }
    start()
  }
}