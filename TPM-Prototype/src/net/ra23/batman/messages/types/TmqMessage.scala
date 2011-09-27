package net.ra23.batman.messages.types

case class TmqMessage(msg: String) extends BasicMessage(msg) {
  val QUOTE = payload;
  val SMLHASH = (if (content(4).endsWith("\n")) content(4).substring(0, content(4).length() - 1) else content(4))
  
  payload = payload + ", "+ SMLHASH

  def getResponseMessage(): String = {
    state+"::s::SERVER_MAC::SERVER_QUOTE::SERVER_SML_HASH"
  }
  
}