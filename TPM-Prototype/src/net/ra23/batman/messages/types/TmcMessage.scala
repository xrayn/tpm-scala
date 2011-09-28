package net.ra23.batman.messages.types
import net.ra23.tpm.config._;
case class TmcMessage(msg: String) extends BasicMessage(msg) {
  val partialDHKey = payload;
  fields("partialDHKey") = partialDHKey;
  def getResponseMessage(): String = {
    state+"::s::SERVER_MAC::"+TPMConfiguration.partialDHKey.toString;
  }

}