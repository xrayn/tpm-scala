package net.ra23.batman.messages.types
import net.ra23.tpm.config._;
import net.ra23.batman.communication._;
import net.ra23.batman.encyrption._;
import net.ra23.batman._;

case class TmdMessage(msg: String) extends BasicMessage(msg) {
  val encryptionKey = payload;

  fields("encryptionKey") = encryptionKey;
  def getResponseMessage(): List[Option[Unicast]] = {
    val encryptedBlowfish = PayloadEncryptor.encryptBlowfish(TPMConfiguration.aesKey, mac);
    if (encryptedBlowfish != None) {
    List(Some(Unicast(state + "::" ++ mac + "::" + state + "::s::" + TPMConfiguration.mac + "::" + encryptedBlowfish.get )))
    } else {
      List(None);
    }
  }
}