package net.ra23.batman.messages.types

case class TmdMessage(msg: String) extends BasicMessage(msg) {
	val encryptionKey=payload;
	fields("encryptionKey") = encryptionKey;
	def getResponseMessage(): String = {
    state+"::s::SERVER_MAC::SERVER_ENCRYPTION_KEY"
  }
}