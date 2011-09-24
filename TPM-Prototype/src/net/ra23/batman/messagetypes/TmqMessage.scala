package net.ra23.batman.messagetypes

case class TmqMessage(msg: String) extends BasicMessage(msg) {
  
}