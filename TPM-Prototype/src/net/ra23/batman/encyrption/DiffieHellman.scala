/*
 * adapted from http://louisbotterill.blogspot.com/2009/01/scala-example-of-diffie-hellman.html
 * 
 */
package net.ra23.batman.encyrption

import scala.util.Random;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

 
object DiffieHellmanKeyExchange {
  /*
   * this key is far too weak for production
   * either use: http://tools.ietf.org/html/rfc5114#section-2.2
   * (key of 3072 Bit is needed, there is none)
   * or use Elliptic_curve_cryptography 
   * (http://en.wikipedia.org/wiki/Elliptic_curve_cryptography)
   */
  val p = BigInt("212104724539190889451952152158338349769") //DiffieHellman.randomPrime(128)
  val g = 5

  val keyCenter = new DiffieHellman(g, p);

  newSecretKey()

  def encryptBlowfish(aesKey: String, peerPubKey: Option[String]): String = {
    setPeerPubKey(BigInt(peerPubKey.get))
    val sharedKey = getSharedKey().toString().getBytes()
     peerPubKey match {
      case None => { "no key" }
      case key: Some[String] => {
        val myKeySpec = new SecretKeySpec(sharedKey, "Blowfish");
        val cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, myKeySpec);
        val ciphertext = cipher.doFinal(aesKey.getBytes())
        val cipherout = new Base64().encodeAsString( ciphertext );
        cipherout
      }
    }
  }
  def decryptBlowfish(aesKey: String, peerPubKey: Option[String]): String = {
    setPeerPubKey(BigInt(peerPubKey.get))
    val sharedKey = getSharedKey().toString().getBytes()
    peerPubKey match {
      case None => { "no key" }
      case key: Some[String] => {
        val myKeySpec = new SecretKeySpec(sharedKey, "Blowfish");
        val cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, myKeySpec);
        val crypted = new Base64().decode(aesKey);
        var plaintext = ""
        cipher.doFinal(crypted).foreach(c=>{ plaintext=plaintext+c.toChar});
        plaintext
      }
    }
  }

  def getAddedAesKey(aesKey: String, peerPubKey: Option[String]): String = {
    setPeerPubKey(BigInt(peerPubKey.get))
    val sharedKey = getSharedKey().toString().getBytes()
     (BigInt(aesKey) + getSharedKey()).toString()
  }
  def getAesKeyFromPayload(payload: String, peerPubKey: Option[String]): String = {
    setPeerPubKey(BigInt(peerPubKey.get))
    val sharedKey = getSharedKey().toByteArray
    (BigInt(payload) - getSharedKey()).toString();
  }
  def newSecretKey() = {
    keyCenter.createSecretKey()
  }
  def getPublicKey(): BigInt = {
    keyCenter.getPublicKey();
  }
  def setPeerPubKey(key: BigInt) = {
    keyCenter.setPeerPublicKey(key)
  }

  def getSharedKey(): BigInt = {
    keyCenter.createSharedKey()
  }
}

object DiffieHellman {

  def randomPrime(n: Int): BigInt = { // return a random value with n digits

    val rnd = new Random();

    BigInt.probablePrime(n, rnd)
  }
}

class DiffieHellman(val g: Int, p: BigInt) {

  var secretKey: BigInt = 0
  var peerPublicKey: BigInt = 0

  def createSecretKey() {
    secretKey = random(128)
    println("secretKey = " + secretKey)
  }

  def setPeerPublicKey(x: BigInt) {
    peerPublicKey = x
  }

  def getPublicKey(): BigInt = {
    doExpMod(g, secretKey, p)
  }

  def random(n: Int): BigInt = { // return a random value with n digits
    val rnd = new Random();
    BigInt(n, rnd)
  }

  def createSharedKey(): BigInt = {
  doExpMod(peerPublicKey, secretKey, p)
  }

  private def doExpMod(x: BigInt): BigInt = {
    println("doExpMod g = " + g + ", x = " + x + ", p = " + p)
    doExpMod(g, x, p)
  }

  private def doExpMod(g: BigInt, x: BigInt, m: BigInt): BigInt = {
    g.modPow(x, m)
  }
}
