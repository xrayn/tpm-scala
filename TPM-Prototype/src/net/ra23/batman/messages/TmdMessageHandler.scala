package net.ra23.batman.messages
import net.ra23.tpm.debugger._;
import net.ra23.batman.messages.types._;
import net.ra23.tpm._;
import net.ra23.tpm.config._;
import net.ra23.batman.communication._;
import net.ra23.tpm.config._;
import net.ra23.batman.encyrption._;
import net.ra23.batman._;

case class TmdMessageHandler(message: TmdMessage, as: String) extends BasicMessageHandler(message, as) {
  def handle(): Boolean = {
    isValid = true;
    isHandled = true;
    isHandled
  }

  def getFollowupMessageAsServer(): Option[Unicast] = {
    TPMDebugger.log("Protocol ended no follow up action needed", "debug")
    None
  }
}