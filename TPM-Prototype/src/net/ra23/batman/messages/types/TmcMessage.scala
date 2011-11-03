package net.ra23.batman.messages.types
import net.ra23.tpm.config._;
import net.ra23.batman.communication._;
case class TmcMessage(msg: String) extends BasicMessage(msg) {
  val partialDHKey = payload;
  fields("partialDHKey") = partialDHKey;
  def getResponseMessage(): Unicast = {
    Unicast(state + "::" + mac + "::" + state + "::s::" + TPMConfiguration.mac + "::" + TPMConfiguration.partialDHKey.toString)
  }
}