package net.ra23.batman.messages
import net.ra23.tpm.debugger._;
import net.ra23.batman.messages.types._;
import net.ra23.tpm._;
import net.ra23.tpm.config._;
import net.ra23.batman.communication._;

case class TmqMessageHandler(message: TmqMessage) extends BasicMessageHandler(message) {
  def handle(): Boolean = {
    isHandled = true;
    true
  }
  def getFollowupMessageAsServer(): Option[Unicast] = {
    Some(Unicast("03::"+message.mac+"::03::c::" + TPMConfiguration.mac + "::"+TPMConfiguration.aesKey))
  }
}