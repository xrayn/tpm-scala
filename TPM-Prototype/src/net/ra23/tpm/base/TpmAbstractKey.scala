package net.ra23.tpm.base

import iaik.tc.tss.api.tspi.TcIRsaKey;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.api.tspi.TcIPolicy;
import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.structs.tsp.TcTssUuid;
import iaik.tc.tss.api.structs.tsp.TcUuidFactory;

import net.ra23.tpm.config._;
import net.ra23.tpm.context._;
import net.ra23.tpm.crypt._;



abstract class TpmAbstractKey {
  /*
   * abstract keytype definition is set in concrete class
   */

  val keyType: Long
  val migrateableType: Long

  /*
   * create and load the key into tpm
   */
  val key: TcIRsaKey = TPMContext.context.createRsaKeyObject(TPMKeymanager.keySize | keyType | migrateableType)
  applyPolicies(key)
  val srk = TPMKeymanager.getSRK()
  println("srk loaded:" + srk)
  key.createKey(srk, null)
  key.loadKey(srk)

  /*
   * generate a new uuid (for registering later)
   */
  var keyUuid = TPMKeymanager.getNewUuid(57005)

  val publicKey: TcIRsaKey = null
  def applyPolicies(key: TcIRsaKey) = {
    TPMPolicy.applyPolicy(TPMPolicy.keyUsgPolicy, key)
    println("UsagePolicy apllied");
    TPMPolicy.applyPolicy(TPMPolicy.keyMigPolicy, key)
    println("MigrationPolicy apllied");
  }


  def getPublicKey() = {
    key.getPubKey()
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