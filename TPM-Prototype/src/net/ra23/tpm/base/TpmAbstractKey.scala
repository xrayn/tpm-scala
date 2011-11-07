package net.ra23.tpm.base

import iaik.tc.tss.api.tspi.TcIRsaKey;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.api.tspi.TcIPolicy;
import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.structs.tsp.TcTssUuid;
import iaik.tc.tss.api.structs.tsp.TcUuidFactory;
import iaik.tc.tss.api.structs.tpm.TcTpmPubkey;
import iaik.tc.tss.api.tspi._;
import net.ra23.tpm.config._;
import net.ra23.tpm.context._;
import net.ra23.tpm.crypt._;
import net.ra23.tpm.debugger._;

abstract class TpmAbstractKey {
  /*
   * abstract keytype definition is set in concrete class
   */

  val keyType: Long
  val migrateableType: Long

  val isSigned = false;
  /*
   * create and load the key into tpm
   */
  val key: TcIRsaKey = TPMContext.context.createRsaKeyObject(TPMKeymanager.keySize | keyType | migrateableType)
  applyPolicies(key)
  val srk = TPMKeymanager.getSRK()
  TPMDebugger.log("srk loaded:" + srk, "debug")
  key.createKey(srk, null)
  key.loadKey(srk)

  /*
   * generate a new uuid (for registering later)
   */
  var keyUuid = TPMKeymanager.getNewUuid(57005)

  val publicKey: TcIRsaKey = null
  def applyPolicies(key: TcIRsaKey) = {
    TPMPolicy.applyPolicy(TPMPolicy.keyUsgPolicy, key)
    TPMDebugger.log("UsagePolicy applied", "debug");
    TPMPolicy.applyPolicy(TPMPolicy.keyMigPolicy, key)
    TPMDebugger.log("MigrationPolicy applied", "debug");
  }

  def getPublicKey() = {
    key.getPubKey()
  }
  def getPublicJavaKey() = {
    val pubEkSpec = new java.security.spec.RSAPublicKeySpec(getModulus, getExponent);
    val pubKeyJava = java.security.KeyFactory.getInstance("RSA").generatePublic(pubEkSpec);
    TPMDebugger.log(pubKeyJava, "debug")
    pubKeyJava
  }
  def getModulus() = {
    val modulus = key.getAttribData(TcTssConstants.TSS_TSPATTRIB_RSAKEY_INFO, TcTssConstants.TSS_TSPATTRIB_KEYINFO_RSA_MODULUS)
    // BigInteger requires a leading sign-byte
    modulus.prepend(TcBlobData.newBYTE((0.asInstanceOf[Byte])))
    new java.math.BigInteger(modulus.asByteArray())
  }
  def getExponent() = {
    var result = new java.math.BigInteger("0");
    try {
      val exponent = key.getAttribData(TcTssConstants.TSS_TSPATTRIB_RSAKEY_INFO, TcTssConstants.TSS_TSPATTRIB_KEYINFO_RSA_EXPONENT)
      // BigInteger requires a leading sign-byte
      exponent.prepend(TcBlobData.newBYTE((0.asInstanceOf[Byte])))
      result = new java.math.BigInteger(exponent.asByteArray())
    } catch {
      case _ => {
        TPMDebugger.log("Using default key: 65537", "debug")
        result = new java.math.BigInteger("65537")
      }
    }
    //println("Expo: "+exponent.toHexStringNoWrap());
    // this is the default exponent
    result
  }
  def getKey() = {
    key
  }
  override def toString() = {
    "Key:\n" +
      "Public: " + getPublicKey().toHexStringNoWrap() + "\n" +
      "Unique: " + keyUuid.toStringNoPrefix()
  }

}