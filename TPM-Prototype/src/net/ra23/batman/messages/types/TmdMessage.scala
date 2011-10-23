package net.ra23.batman.messages.types
import net.ra23.tpm.config._;
import net.ra23.batman.communication._;

case class TmdMessage(msg: String) extends BasicMessage(msg) {
	val encryptionKey=payload;
	
	fields("encryptionKey") = encryptionKey;
	def getResponseMessage(): Unicast = {
    Unicast(state+"::"++mac+"::"+state+"::s::"+TPMConfiguration.mac+"::SERVER_ENCRYPTION_KEY")
  }
}