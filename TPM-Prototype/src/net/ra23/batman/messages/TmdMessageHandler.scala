package net.ra23.batman.messages
import net.ra23.tpm.debugger._;
import net.ra23.batman.messages.types._;
import net.ra23.tpm._;
import net.ra23.tpm.config._;
import net.ra23.batman.communication._;

case class TmdMessageHandler(message: TmdMessage) extends BasicMessageHandler(message) {
  def handle(): Boolean = {
    true
  }
  def getFollowupMessageAsServer(): Option[Unicast] = {
    TPMDebugger.log("Protocol ended no follow up action needed", "debug")
    None
  }
}