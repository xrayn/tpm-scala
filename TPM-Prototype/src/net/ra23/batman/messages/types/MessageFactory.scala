package net.ra23.batman.messages.types

object MessageFactory {
  def apply(message: String ): BasicMessage = {
    message match {
      case msg: String if msg.startsWith("01::") => TmcMessage(msg)
      case msg: String if msg.startsWith("02::") => TmqMessage(msg)
      case msg: String if msg.startsWith("03::") => TmdMessage(msg)
      case _ => println("error"); null
    } 
  }  
}
