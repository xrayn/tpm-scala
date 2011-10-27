/*
 * adapted from http://louisbotterill.blogspot.com/2009/01/scala-example-of-diffie-hellman.html
 * 
 */
package net.ra23.batman.encyrption

import scala.util.Random;

object DiffieHellmanKeyExchange {

  val p = BigInt("212104724539190889451952152158338349769") //DiffieHellman.randomPrime(128)
  val g = 5

  val keyCenter = new DiffieHellman(g, p);
  
  newSecretKey()
  
  /*
   * This is a very simple implementation by adding and substracting the keys.
   * This means only numbers are allowed here for the aes key.
   * 
   * The problem:
   * The aes key is saved in a char array in the kernel module of length 16.
   * (crypto api uses such a string as default)
   * Because we need to transform the aes key here into a number to do some math, our
   * result is again a 16 sign number as a result, limiting our keyspace to only 10 signs.
   * Although the possible keyspace in the kernel module is 2^128 Bit (2^(16*8Bit)) with
   * each chars going from 0-255 we only use a fraction of 0-9 of that possible space.
   * 
   * This limits our keyspace to to 2^(16*ld(10)Bit) = 2^(16*3.321928095Bit)  = 2^53.15084952Bit
   * 
   * 
   */
  
  def getAddedAesKey(aesKey: String): String = {
	(BigInt(aesKey)+getSharedKey()).toString()
  }
  def getAesKeyFromPayload(payload: String): String = {
    (BigInt(payload)-getSharedKey()).toString();
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
