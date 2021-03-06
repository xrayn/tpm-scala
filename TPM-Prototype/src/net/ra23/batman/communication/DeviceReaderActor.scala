package net.ra23.batman.communication

import scala.actors.Actor
import java.io._
import java.nio._
import java.nio.charset._
import java.util._
import net.ra23.batman.messages.fragmented._
import scala.actors.Actor._
import java.io._
import net.ra23.tpm.debugger._
import net.ra23.batman.communication._
import java.lang.ProcessBuilder
import scala.sys.process.Process
import net.ra23.batman.messages.types.BasicMessage
import scala.tools.nsc.dependencies.Files
import java.nio.channels._

object DeviceReaderActor extends Actor {
  //val file = "/dev/mcom"

  val test = new Array[Byte](1000)
  var file = ""
  var device: RandomAccessFile = null;
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
    //val fiS = new FileInputStream(file)

    //    while (fiS.available() == 0) {
    //      Thread.sleep(10)
    //      
    //      TPMDebugger.log("no data sleeping ... ", "debug");
    //    }
    device = new RandomAccessFile(file, "rw");
    val len = device.read(test)
    //unlockFile()
    val tmp = new Array[Byte](len)
    test.copyToArray(tmp)
    result = new String(tmp);
    // tmp fix for reading from chardevice!
    TPMDebugger.log("reading " + file + "[" + result + "]", "debug");
    device.close()
    result
  }
  def act = loop {
    TPMDebugger.log("Starting device reader @[" + file + "]");
    // clean up

    loop {
      val message = read();
      if (FragmentedMessageStorage.isFragmentedMessage(message)) {
        FragmentedMessageStorage.insertAndMerge(message) match {
          case msg: Some[String] => MsgDispatcher ! msg.get
          case None => TPMDebugger.log("message not ready yet", "debug");
          case _ =>
        }
      } else {
        MsgDispatcher ! message
      }
    }
  }
  def apply(filename: String) {
    //createListenerFile(filename);    
    file = filename;
    var run = true
    val test = new Array[Byte](1000);
    var count =0;
    device = new RandomAccessFile(file, "r");
    while (run) {
      val time = System.nanoTime();
      device.read(test)
      count = count +1
      if ((System.nanoTime() - time)>100000000) {
        run = false;
      }
    }
    device.close
    println("Skipped ["+count+"] lines")
    start()

  }
  def createListenerFile(filename: String) {
    if (new File(filename).exists()) {
      TPMDebugger.log(getClass().getSimpleName() + ": removing " + filename + "", "debug");
      val pb = Process("""rm -f """ + filename)
      pb.!
    }
    if (new File(filename + ".lock").exists()) {
      TPMDebugger.log(getClass().getSimpleName() + ": removing " + filename + ".lock", "debug");
      val pb = Process("""rm -f """ + filename + ".lock")
      //val pb = new ProcessBuilder("rm", "-f", );
      pb.!
    }
    TPMDebugger.log(getClass().getSimpleName() + ": creating new " + filename + "", "debug");
    val pb = new ProcessBuilder("mkfifo", filename);
    pb.start();
  }
}