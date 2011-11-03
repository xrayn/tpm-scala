package net.ra23.batman.messages.types

import net.ra23.tpm.config._;
import net.ra23.tpm.sign.TPMSigning;
import net.ra23.batman.communication._;

case class TmqMessage(msg: String) extends BasicMessage(msg) {
  val QUOTE = payload;
  val SMLHASH = (if (content(4).endsWith("\n")) content(4).substring(0, content(4).length() - 1) else content(4))

  fields("quote") = QUOTE;
  fields("smlhash") = SMLHASH;

  payload = payload + ", " + SMLHASH

  def getResponseMessage(): Unicast = {
    Unicast(state + "::" + mac + "::" + state + "::s::" + TPMConfiguration.mac + "::" + TPMSigning.getQuoteBase64() + "::SERVER_SML_HASH")
  }

}