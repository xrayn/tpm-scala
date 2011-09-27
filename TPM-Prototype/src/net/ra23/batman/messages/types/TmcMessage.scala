package net.ra23.batman.messages.types

case class TmcMessage(msg: String) extends BasicMessage(msg) {
  val partialDHKey = payload;
  
  def getResponseMessage(): String = {
    state+"::s::SERVER_MAC::SERVER_PARTIAL_DH_KEY"
  }

}