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


object TPMain {
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
  println(TPMContext.context.isConnected());
  /**
   * The TPM object.
   */
  val tpm = TPMContext.context.getTpmObject()
  if (tpm == null || TPMContext.context == null) {
    println("tpm or  == null");
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

  def main(args: Array[String]): Unit = {
    // println(getTpmVersion.toString())
    // println(tpmManufactuerIs(TPM_MAN_ETHZ))
    //initAndLoadStorageRootKey()
    //println(getRandom())
    val key=TpmSigningKey();
    println("key:"+key)
    val aKey = getNewCertifiedKey()
    println(uuids);
    TPMContext.context.loadKeyByBlob(srk_, getKeyBlobData(aKey))
    TPMContext.context.getRegisteredKeysByUuidSystem(uuids.head._2).foreach(println);
    val destKey = migrateKey()
    val aKey2 = TpmBindingKey();
    //encrypt(getKeyNew("b"));
    exportPublicKey(aKey2.getKey, "/tmp/foo");
    TPMCrypto.decrypt(TPMCrypto.encrypt(importPublicKey("/tmp/foo"), "I AM A TEST"), aKey2.getKey)

  }
  def getTpmVersion() = {
    var tpmVersion: TcTpmVersion = null

    var result = tcs_.TcsipGetCapability(hContext_, TcTpmConstants.TPM_CAP_VERSION_VAL, null)
    var retVal: TcBlobData = result(1).asInstanceOf[TcBlobData]
    tpmVersion = (new TcTpmCapVersionInfo(retVal)).getVersion()
    var tssVersion = new TcTssVersion();

    try {
      tssVersion.setMajor(tpmVersion.getMajor())
      tssVersion.setMinor(tpmVersion.getMinor())
      tssVersion.setRevMajor(tpmVersion.getRevMajor())
      tssVersion.setRevMinor(tpmVersion.getRevMinor())
    } catch {
      case e: java.lang.NullPointerException => e.printStackTrace()
      case e: Exception => e.printStackTrace();
    }
    tssVersion
  }
  def tpmManufactuerIs(man: TcBlobData): Boolean = {
    val subCap: TcBlobData = TcBlobData.newUINT32(TcTpmConstants.TPM_CAP_PROP_MANUFACTURER)
    val result = tcs_.TcsipGetCapability(hContext_, TcTpmConstants.TPM_CAP_PROPERTY, subCap)
    result(1).asInstanceOf[TcBlobData].toHexString() == man.toHexString()
  }

  def getRandom(): Long = {
    tpm.getRandom(1.asInstanceOf[Long]).asShortArray()(0).asInstanceOf[Long]
  }


  def getNewCertifiedKey(): TcIRsaKey = {

    val pcrComp: TcIPcrComposite = null;

    val storageKey: TcIRsaKey = TPMContext.context.createRsaKeyObject(TcTssConstants.TSS_KEY_TYPE_STORAGE
      | TcTssConstants.TSS_KEY_SIZE_2048 | TcTssConstants.TSS_KEY_AUTHORIZATION);
    keyUsgPolicy.assignToObject(storageKey);
    keyMigPolicy.assignToObject(storageKey);
    storageKey.createKey(srk_, pcrComp);
    storageKey.loadKey(srk_);

    val certifyKey: TcIRsaKey = TPMContext.context.createRsaKeyObject(TcTssConstants.TSS_KEY_SIZE_2048
      | TcTssConstants.TSS_KEY_TYPE_SIGNING | TcTssConstants.TSS_KEY_MIGRATABLE |
      TcTssConstants.TSS_KEY_AUTHORIZATION);
    keyUsgPolicy.assignToObject(certifyKey);
    keyMigPolicy.assignToObject(certifyKey);
    certifyKey.createKey(srk_, pcrComp);
    certifyKey.loadKey(srk_);

    val keyUuid = getNewUuid(57005)
    TPMContext.context.registerKey(
      certifyKey,
      TcTssConstants.TSS_PS_TYPE_SYSTEM,
      keyUuid,
      TcTssConstants.TSS_PS_TYPE_SYSTEM,
      TcUuidFactory.getInstance().getUuidSRK());
    uuids += keyUuid.toStringNoPrefix() -> keyUuid
    certifyKey
  }
  protected def getNewUuid(prefix: Int = 0): TcTssUuid = {
    val keyUuid = new TcTssUuid().init(prefix, 0, 0, 0.asInstanceOf[Short], 0.asInstanceOf[Short], TPMContext.context.getTpmObject().getRandom(6).asShortArray());
    keyUuid
  }
  def getKeyBlobData(someKey: TcIRsaKey): TcBlobData = {
    val bd: TcBlobData = someKey.getAttribData(TcTssConstants.TSS_TSPATTRIB_KEY_BLOB,
      TcTssConstants.TSS_TSPATTRIB_KEYBLOB_BLOB);
    TPMContext.context.loadKeyByBlob(srk_, bd)
    bd
  }
  def migrateKey(): TcIRsaKey = {
    // create the key of the migration authority
    val maKey = TPMContext.context.createRsaKeyObject(TcTssConstants.TSS_KEY_SIZE_2048
      | TcTssConstants.TSS_KEY_TYPE_MIGRATE | TcTssConstants.TSS_KEY_AUTHORIZATION);
    keyUsgPolicy.assignToObject(maKey);
    keyMigPolicy.assignToObject(maKey);
    maKey.createKey(srk_, null);

    // create the key (data) to migrate from
    val srcKey = TPMContext.context.createRsaKeyObject(TcTssConstants.TSS_KEY_SIZE_2048
      | TcTssConstants.TSS_KEY_TYPE_BIND | TcTssConstants.TSS_KEY_MIGRATABLE | TcTssConstants.TSS_KEY_AUTHORIZATION);
    keyUsgPolicy.assignToObject(srcKey);
    keyMigPolicy.assignToObject(srcKey);
    srcKey.createKey(srk_, null);

    // authorize the migration authority to be used and create migration blob
    val keyAuth = tpm.authorizeMigrationTicket(maKey, TcTssConstants.TSS_MS_MIGRATE);
    val out = srcKey.createMigrationBlob(srk_, keyAuth);
    val random = out(0); // send this to the destination
    val migData = out(1); // send this to Migration Authority

    // create the migration data key object
    val migDataKey = TPMContext.context.createRsaKeyObject(TcTssConstants.TSS_KEY_TYPE_LEGACY);
    migDataKey.setAttribData(TcTssConstants.TSS_TSPATTRIB_KEY_BLOB,
      TcTssConstants.TSS_TSPATTRIB_KEYBLOB_BLOB, migData);

    // migrate the data (key)
    maKey.loadKey(srk_);
    pubKey.loadKey(srk_);
    maKey.migrateKey(pubKey, migDataKey);

    val migratedData = migDataKey.getAttribData(TcTssConstants.TSS_TSPATTRIB_KEY_BLOB,
      TcTssConstants.TSS_TSPATTRIB_KEYBLOB_BLOB);

    // create an key object for the migrated key
    val destKey = TPMContext.context.createRsaKeyObject(TcTssConstants.TSS_KEY_SIZE_2048
      | TcTssConstants.TSS_KEY_TYPE_BIND | TcTssConstants.TSS_KEY_MIGRATABLE | TcTssConstants.TSS_KEY_NO_AUTHORIZATION);

    // convert the migration blob to create a normal wrapped key
    destKey.convertMigrationBlob(pubKey, random, migratedData);

    println(out(0).toHexString());
    // create encdata object and test decrption

    destKey
    //maybe an TcBlobData has to be used for transporting to destination
    // this key is used at the destination to encrypt data
  }

  /*
   * dont know if this is needed!
   */
  def loadMigrationBlog(publicKey: TcIRsaKey, random: TcBlobData, migrationBlob: TcBlobData): TcIRsaKey = {
    val encKey = TPMContext.context.createRsaKeyObject(TcTssConstants.TSS_KEY_SIZE_2048
      | TcTssConstants.TSS_KEY_TYPE_BIND | TcTssConstants.TSS_KEY_MIGRATABLE | TcTssConstants.TSS_KEY_AUTHORIZATION);
    encKey.convertMigrationBlob(publicKey, random, migrationBlob)
    keyUsgPolicy.assignToObject(encKey)
    //TPMContext.context.loadKeyByBlob(srk_, migrationBlob);
    encKey

  }
  def exportPublicKey(aKey: TcIRsaKey, filename: String = "/tmp/key.pub") {
    val file = new File(filename);
    val foStream = new FileOutputStream(file);
    val oStream = new ByteArrayOutputStream();
    val bs = aKey.getPubKey().asByteArray()
    //Writes a byte to the byte array output stream.
    oStream.write(bs);
    oStream.writeTo(foStream);
    println("Key written into the file " + "/tmp/key.pub");
    foStream.close();
  }
  def importPublicKey(filename: String): TcBlobData = {
    val file = new File(filename);
    val fiStream = new FileInputStream(file);

    val length = file.length();
    println("Reading file " + filename + " [" + length + "] Bytes")
    val data = new Array[Byte](length.asInstanceOf[Int])
    fiStream.read(data);
    fiStream.close();
    TcBlobData.newByteArray(data)
  }

}
