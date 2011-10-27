package net.ra23.batman.messages
import net.ra23.tpm.debugger._;
import net.ra23.batman.messages.types._;
import net.ra23.tpm._;
import net.ra23.tpm.config._;
import net.ra23.batman.communication._;

case class TmcMessageHandler(message: TmcMessage, as :String) extends BasicMessageHandler(message, as) {
  def handle(): Boolean = {
    isHandled = true;
    true
  }

  def getFollowupMessageAsServer(): Option[Unicast] = {
    Some(Unicast("02::" + message.mac + "::02::c::" + TPMConfiguration.mac + "::CLIENT_QUOUTE::CLIENT_SML_HASH"))
  }
}