package net.ra23.batman

object SocketTest {

  def main(args: Array[String]): Unit = {
    val manager = ManagementActor(None);
    //val connector = SocketConnector();
    //connector.run();
    while (true) {
        val command = scala.Console.readLine("Type your command:")
        manager ! command
    }
    
  }

}