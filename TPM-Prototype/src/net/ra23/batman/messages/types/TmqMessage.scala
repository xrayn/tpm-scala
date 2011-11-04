package net.ra23.batman.messages.types

import net.ra23.tpm.config._;
import net.ra23.tpm.sign.TPMSigning;
import net.ra23.batman.communication._;
import net.ra23.helper.PayloadHelper;

case class TmqMessage(msg: String) extends BasicMessage(msg) {
  val QUOTE = payload;
  val SMLHASH = (if (content(4).endsWith("\n")) content(4).substring(0, content(4).length() - 1) else content(4))

  fields("quote") = QUOTE;
  fields("smlhash") = SMLHASH;

  payload = payload + ", " + SMLHASH

  def getResponseMessage(): List[Option[Unicast]] = {
    var result =  List[Option[Unicast]]()
    // tune 512 to a higher parameter, this is only for testing!
    for (payload <- PayloadHelper.splitPayload(TPMSigning.getQuoteBase64() + "::CLIENT_SML_HASH", 512))
      result=  Some(Unicast(state + "::" + mac + "::" + state + "f::s::" + TPMConfiguration.mac + "::" + payload))::result
    result.reverse
  }

}