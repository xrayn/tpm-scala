package net.ra23.batman.messages
import net.ra23.tpm.debugger._;
import net.ra23.batman.messages.types._;
import net.ra23.tpm._;
import net.ra23.tpm.config._;
import net.ra23.batman.communication._;

abstract class BasicMessageHandler(message: BasicMessage) {
  var isValid = true
  insertIntoConnectionStorage()
  def handle(): Boolean

  def insertIntoConnectionStorage() {
    if (isValid) {
      net.ra23.batman.ConnectionStorage.update(message.mac, message)
    }
  }
  def getFollowupMessageAsClient(): Unicast = {
    message.getResponseMessage()
  }
  def getFollowupMessageAsServer(): Option[Unicast]
}