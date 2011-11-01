package net.ra23.tpm.base
import java.math._;
import java.util.SortedMap;
import java.util.TreeMap;
import iaik.tc.tss.api.constants.tcs.TcTcsErrors;
import iaik.tc.tss.api.constants.tpm.TcTpmConstants;
import iaik.tc.tss.api.constants.tpm.TcTpmErrors;
import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.constants.tsp.TcTssErrors;
import iaik.tc.tss.api.exceptions.tcs.TcTcsException;
import iaik.tc.tss.api.exceptions.tcs.TcTddlException;
import iaik.tc.tss.api.exceptions.tcs.TcTpmException;
import iaik.tc.tss.api.structs.common.TcBasicTypeDecoder;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.api.structs.tcs.TcTcsAuth;
import iaik.tc.tss.api.structs.tpm.TcTpmAuthdata;
import iaik.tc.tss.api.structs.tpm.TcTpmCapVersionInfo;
import iaik.tc.tss.api.structs.tpm.TcTpmEncauth;
import iaik.tc.tss.api.structs.tpm.TcTpmNonce;
import iaik.tc.tss.api.structs.tpm.TcTpmSecret;
import iaik.tc.tss.api.structs.tpm.TcTpmVersion;
import iaik.tc.tss.api.structs.tsp.TcTssVersion;
import iaik.tc.tss.impl.csp.TcCrypto;
import iaik.tc.tss.impl.java.tsp.tcsbinding.TcITcsBinding;
import iaik.tc.tss.impl.java.tsp.tcsbinding.local.TcTcsBindingLocal;
import iaik.tc.tss.impl.java.tsp.tcsbinding;
import iaik.tc.tss.api.exceptions.common.TcTssException;
import iaik.tc.tss.api.tspi.TcIContext;
import iaik.tc.tss.api.tspi.TcITpm;
import iaik.tc.tss.api.tspi._;
import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.exceptions.common.TcTssException;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.api.structs.tsp.TcTssUuid;
import iaik.tc.tss.api.structs.tsp.TcUuidFactory;
import iaik.tc.tss.api.tspi.TcIContext;
import iaik.tc.tss.api.tspi.TcIPcrComposite;
import iaik.tc.tss.api.tspi.TcIPolicy;
import iaik.tc.tss.api.tspi.TcIRsaKey;
import iaik.tc.tss.api.tspi.TcITpm;
import iaik.tc.tss.api.structs.tpm.TcTpmPubkey;
import iaik.tc.tss.api.structs.tsp.TcTssValidation;
import iaik.tc.tss.impl.csp.TcBasicCrypto;
import iaik.tc.tss.impl.csp.TcCrypto;
import scala.collection.mutable._;
import scala.io._;
import java.io._;
import net.ra23.tpm.config._;
import net.ra23.tpm.context._;
import net.ra23.tpm.crypt._;
import net.ra23.tpm.debugger._;

case class TPM {
  
}
object TPM {
  val config = TPMConfiguration.fromXmlFile("/tmp/config.xml")
  var key: TcIRsaKey = null;
  val tcs_ = new TcTcsBindingLocal()
  val TPM_MAN_ETHZ: TcBlobData = TcBlobData.newStringASCII("ETHZ")
  val hContext_ = tcs_.TcsiOpenContext()(1).asInstanceOf[Long]
  var uuids = HashMap.empty[String, TcTssUuid]

  /**
   * The TPM context.
   */
  TPMContext.context.connect();
  TPMDebugger.log(TPMContext.context.isConnected());
  /**
   * The TPM object.
   */
  val tpm = TPMContext.context.getTpmObject()
  if (tpm == null || TPMContext.context == null) {
    TPMDebugger.log("tpm or  == null");
  }

  var keyUuid: TcTssUuid = null;

  val srkSecret: TcBlobData = TcBlobData.newString(TPMConfiguration.get("srkPassword"), false, TPMConfiguration.get("pwdEncoding"));
  val tpmSecret: TcBlobData = TcBlobData.newString(TPMConfiguration.get("tpmPassword"), false, TPMConfiguration.get("pwdEncoding"));
  val keySecret: TcBlobData = TcBlobData.newString(TPMConfiguration.get("keyPassword"), false, TPMConfiguration.get("pwdEncoding"));
  val srk_ = TPMContext.context.getKeyByUuid(TcTssConstants.TSS_PS_TYPE_SYSTEM,
    TcUuidFactory.getInstance().getUuidSRK());

  // alternative mechanism to get SRK instance
  // srk_ = context_.createRsaKeyObject(TcTssConstants.TSS_KEY_TSP_SRK);

  // set SRK policy
  val srkPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
  srkPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN, srkSecret);
  srkPolicy.assignToObject(srk_);

  // setup TPM policy
  val tpmPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
  tpmPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN, tpmSecret);
  tpmPolicy.assignToObject(tpm);
  // create a key usage policy for this key
  val keyUsgPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
  keyUsgPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN, keySecret);

  //create a key migration policy for this key
  val keyMigPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_MIGRATION);
  keyMigPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN, keySecret);

  // this is currently the pubkey of this client.
  // and so used for decryption.
  // in migrateKey this key is used to wrap the migrationkey (which is used for encryption at the destination)

  val pubKey = TPMContext.context.createRsaKeyObject(TcTssConstants.TSS_KEY_SIZE_2048
    | TcTssConstants.TSS_KEY_TYPE_STORAGE | TcTssConstants.TSS_KEY_NO_AUTHORIZATION);
  keyUsgPolicy.assignToObject(pubKey);
  keyMigPolicy.assignToObject(pubKey);
  pubKey.createKey(srk_, null);

  
  def test(): Unit = {
    // println(getTpmVersion.toString())
    // println(tpmManufactuerIs(TPM_MAN_ETHZ))
    //initAndLoadStorageRootKey()
    //println(getRandom())
    val key = TpmSigningKey();
    //    println("key:"+key)
    //    val aKey = getNewCertifiedKey()
    //    println(uuids);
    //    TPMContext.context.loadKeyByBlob(srk_, getKeyBlobData(aKey))
    //    TPMContext.context.getRegisteredKeysByUuidSystem(uuids.head._2).foreach(println);
    //    //val destKey = migrateKey()
    val aKey1 = TPMKeymanager.getBindingKey(TPMKeymanager.createBindingKey())
    val aKey2 = TPMKeymanager.getBindingKey(TPMKeymanager.createBindingKey())
    println(TPMKeymanager.bindingKeyDb)
    //encrypt(getKeyNew("b"));
    TPMKeymanager.exportPublicKey(aKey2, "/tmp/foo");
    //TPMCrypto.decrypt(TPMCrypto.encrypt(TPMKeymanager.importPublicKey("/tmp/foo"), "I AM A TEST"), aKey2.getKey)
    TPMCrypto.decrypt(TPMCrypto.encryptByTpm(aKey2, "I AM A TEST 1"), aKey2)
    TPMCrypto.decrypt(TPMCrypto.encrypt(aKey2, "I AM A TEST 2"), aKey2)
    TPMCrypto.decrypt(TPMCrypto.encrypt(aKey2, "I AM A TEST 3"), aKey2)
    TPMCrypto.decrypt(TPMCrypto.encrypt(aKey2, "I AM A TEST 4"), aKey2)

  }

}