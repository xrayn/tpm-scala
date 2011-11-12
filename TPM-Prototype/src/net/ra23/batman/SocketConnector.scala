package net.ra23.batman

import java.io._
import java.net.{ InetAddress, ServerSocket, Socket, SocketException }
import java.util.Random

case class SocketConnector {
  def run() = {
    try {
      val listener = new ServerSocket(9999);
      while (true)
        new ServerThread(listener.accept()).start();
      listener.close()
    } catch {
      case e: IOException =>
        System.err.println("Could not listen on port: 9999.");
        System.exit(-1)
    }
  }
}

case class ServerThread(socket: Socket) extends Thread("ServerThread") {
  val out = new OutputStreamWriter(socket.getOutputStream());
  override def run(): Unit = {
    val rand = new Random(System.currentTimeMillis());
    try {
      val in = new InputStreamReader(socket.getInputStream());
      val bin = new BufferedReader(in)
      val manager = ManagementActor(Some(out));
      while (true) {
        val command = bin.readLine();
        manager ! command
      }
      out.close();
      in.close();
    } catch {
      case e: SocketException =>
        () // avoid stack trace when stopping a client with Ctrl-C
      case e: IOException =>
        e.printStackTrace();
    } finally {
      socket.close();
    }
  }

}
  


