package net.ra23.batman.messages
import net.ra23.tpm.debugger._;
import net.ra23.batman.measurement._;
import net.ra23.batman.messages.types._;
import net.ra23.tpm._;
import net.ra23.tpm.config._;
import net.ra23.batman.communication._;

abstract class BasicMessageHandler(message: BasicMessage, as: String) {
  var isHandled = false;
  var isValid = true
  var encryptedAesKey = "";
  _start();

  def _start() = {
    startMeasurement()
    insertIntoConnectionStorage()
    handle()
    endMeasurement()
  }
  def handle(): Boolean
  def startMeasurement() = {
    MessageMeasurer.measure(message.mac + "," + getClass().getSimpleName().toUpperCase(), "start", as);
  }
  def endMeasurement() = {
    MessageMeasurer.measure(message.mac + "," + getClass().getSimpleName().toUpperCase(), "end", as);
  }
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