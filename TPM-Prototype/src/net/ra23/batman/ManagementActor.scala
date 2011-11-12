package net.ra23.batman

import scala.actors.Actor
import scala.actors.Actor._
import java.io._
import java.net.{ InetAddress, ServerSocket, Socket, SocketException }

case class ManagementActor(outOption: Option[OutputStreamWriter]) extends Actor {
  start();
  consoleHelp();
  def act = loop {
    react {
      case msg: String => {
        commandDispatcher(msg)
        consoleHelp();
      }
      case _ => Thread.sleep(1000); println("aeh what?");
    }
  }
  def commandDispatcher(command: String) {
    command match {
      case c: String if command == "p" => {
        printTable()
      }

      case c: String if command == "t" => {
        injectTmqMessage()
      }
      case c: String if command == "e" => {
        exportPublicSrk()
      }

      case c: String if command == "connect" => {
        connect();
      }
      case c: String if command.startsWith("autoconnect") => {
        val cmd = command.replace("autoconnect", "").trim();
        println(cmd);

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

  }
  def socketWrite(what: String) {
    outOption match {
      case out: Some[OutputStreamWriter] => {
        out.get.write(what + "\n");
        out.get.flush();
      }
      case None => {
        println(what);
      }
    }

  }
  def consoleHelp() = {
    socketWrite("Command Overview:")
    socketWrite("[h] -> print this help")
    socketWrite("[e] -> export public SRK")
    socketWrite("[c] -> change dh key + aes key")
    socketWrite("[t] -> inject a tmq package (start protocol)")
    socketWrite("[p] -> show current state tables and keydb")
    socketWrite("[s] -> show stat summary")
    socketWrite("[connect] -> connect all unconnected nodes")
    socketWrite("[autoconnect (start|stop|restart)] -> start auto connection actor")
  }
  def exportPublicSrk() {
    socketWrite("[Exporting public SRK ......]")

  }
  def injectTmqMessage(): Unit = {
    socketWrite("[Testing tmq & tmd ......]")

  }
  def connect(): Unit = {

    socketWrite("Connecting");

    Thread.sleep(2000);
    socketWrite("")
    printStats()
    socketWrite("")
  }
  def printStats() {
    socketWrite("Known Nodes: [" + ConnectionStorage.keyDb.size + "]");
    socketWrite("Unconnected Nodes: [" + ConnectionStorage.getNotInState3().length + "]");
    socketWrite("Connected Nodes: [" + (ConnectionStorage.keyDb.size - ConnectionStorage.getNotInState3().length) + "]");
  }
  def printTable(): Unit = {
    socketWrite("State1")
    socketWrite(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state1")))
    socketWrite("State2")
    socketWrite(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state2")))
    socketWrite("State3")
    socketWrite(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.asList("state3")))
    socketWrite("KeyDb")
    socketWrite(net.ra23.batman.Tabulator.format(net.ra23.batman.ConnectionStorage.keyDbasList()))
  }

}

  
