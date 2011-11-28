package net.ra23.batman.messages
import net.ra23.tpm.debugger._;
import net.ra23.tpm.sign.TPMSigning;
import net.ra23.batman._;
import net.ra23.batman.encyrption._;
import net.ra23.batman.messages.types._;
import net.ra23.tpm._;
import net.ra23.tpm.base._;
import net.ra23.tpm.config._;
import net.ra23.batman.communication._;

case class TmqMessageHandler(message: TmqMessage, as: String) extends BasicMessageHandler(message, as) {
  def handle(): Boolean = {
    isValid = checkQoute();
    isHandled = true;
    isHandled
  }

  def getFollowupMessageAsServer(): List[Option[Unicast]] = {
    isValid match {
      case iv: Boolean if iv => {
        val encryptedBlowfish = PayloadEncryptor.encryptBlowfish(TPMConfiguration.aesKey, message.mac);
        if (encryptedBlowfish != None) {
          List(Some(Unicast("03::" + message.mac + "::03::c::" + TPMConfiguration.mac + "::" + encryptedBlowfish.get)))
        } else {
          List(None);
        }
      }
      case iv: Boolean if !iv => List(None)
    }

  }

  def checkQoute(): Boolean = {
    val akey = TPMKeymanager.createRsaKeyObject(TPMKeymanager.importPublicKey(TPMConfiguration.get("signingKeyPath") + message.mac + ".key.pub"))
    TPMSigning.verifyCertifiedNonce(message.QUOTE, akey)
  }
}