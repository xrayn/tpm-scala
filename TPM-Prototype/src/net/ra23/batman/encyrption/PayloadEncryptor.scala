package net.ra23.batman.encyrption
import net.ra23.batman._;
import net.ra23.batman.messages.types._;
import net.ra23.tpm.config._;

object PayloadEncryptor {

  def encryptBlowfish(plaintext: String, key: String): String = {
    DiffieHellmanKeyExchange.encryptBlowfish(plaintext, ConnectionStorage.getPeerKey(key))
  }
  
  def decryptBlowfish(ciphertext: String, key: String): String = {
    DiffieHellmanKeyExchange.decryptBlowfish(ciphertext, ConnectionStorage.getPeerKey(key))
  }
  
}