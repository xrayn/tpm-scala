package net.ra23.tpm.base;

import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.api.structs.tsp.TcTssUuid;
import iaik.tc.tss.api.structs.tsp.TcUuidFactory;
import iaik.tc.tss.api.tspi.TcIRsaKey;
import scala.collection.mutable._;
import scala.io._;
import java.io._;
import net.ra23.tpm.config._;
import net.ra23.tpm.context._;
import net.ra23.tpm.crypt._;
import scala.collection.mutable._;
//import org.apache.log4j.Logger;
import net.ra23.tpm.debugger._;

object TPMKeymanager {

  val srk_ = TPMContext.context.getKeyByUuid(TcTssConstants.TSS_PS_TYPE_SYSTEM,
    TcUuidFactory.getInstance().getUuidSRK());
  val tpm = TPMContext.context.getTpmObject()
  /*
   * size definition 
   */
  val keySize = TcTssConstants.TSS_KEY_SIZE_2048;
  val bindingKeyDb = Map.empty[String, TpmBindingKey]

  if (tpm == null || TPMContext.context == null) {
    println("tpm or  == null");
  }
  /*
   * apply the policies 
   */
  TPMPolicy.applyPolicy(TPMPolicy.srkPolicy, srk_)
  TPMDebugger.log("SRK Policy applied to srk")
  TPMPolicy.applyPolicy(TPMPolicy.tpmPolicy, tpm)
  TPMDebugger.log("SRK Policy applied to tpm")

  //  @deprecated def migrateKey(): TcIRsaKey = {
  //    // create the key of the migration authority
  //    val maKey = TPMContext.context.createRsaKeyObject(TcTssConstants.TSS_KEY_SIZE_2048
  //      | TcTssConstants.TSS_KEY_TYPE_MIGRATE | TcTssConstants.TSS_KEY_AUTHORIZATION);
  //    TPMPolicy.keyUsgPolicy.assignToObject(maKey);
  //    TPMPolicy.keyMigPolicy.assignToObject(maKey);
  //    maKey.createKey(srk_, null);
  //
  //    // create the key (data) to migrate from
  //    val srcKey = TPMContext.context.createRsaKeyObject(TcTssConstants.TSS_KEY_SIZE_2048
  //      | TcTssConstants.TSS_KEY_TYPE_BIND | TcTssConstants.TSS_KEY_MIGRATABLE | TcTssConstants.TSS_KEY_AUTHORIZATION);
  //    TPMPolicy.keyUsgPolicy.assignToObject(srcKey);
  //    TPMPolicy.keyMigPolicy.assignToObject(srcKey);
  //    srcKey.createKey(srk_, null);
  //
  //    // authorize the migration authority to be used and create migration blob
  //    val keyAuth = tpm.authorizeMigrationTicket(maKey, TcTssConstants.TSS_MS_MIGRATE);
  //    val out = srcKey.createMigrationBlob(srk_, keyAuth);
  //    val random = out(0); // send this to the destination
  //    val migData = out(1); // send this to Migration Authority
  //
  //    // create the migration data key object
  //    val migDataKey = TPMContext.context.createRsaKeyObject(TcTssConstants.TSS_KEY_TYPE_LEGACY);
  //    migDataKey.setAttribData(TcTssConstants.TSS_TSPATTRIB_KEY_BLOB,
  //      TcTssConstants.TSS_TSPATTRIB_KEYBLOB_BLOB, migData);
  //
  //    val pubKey = TPMContext.context.createRsaKeyObject(TcTssConstants.TSS_KEY_SIZE_2048
  //      | TcTssConstants.TSS_KEY_TYPE_STORAGE | TcTssConstants.TSS_KEY_NO_AUTHORIZATION);
  //    TPMPolicy.keyUsgPolicy.assignToObject(pubKey);
  //    TPMPolicy.keyMigPolicy.assignToObject(pubKey);
  //    pubKey.createKey(srk_, null);
  //    // migrate the data (key)
  //    maKey.loadKey(srk_);
  //    pubKey.loadKey(srk_);
  //    maKey.migrateKey(pubKey, migDataKey);
  //
  //    val migratedData = migDataKey.getAttribData(TcTssConstants.TSS_TSPATTRIB_KEY_BLOB,
  //      TcTssConstants.TSS_TSPATTRIB_KEYBLOB_BLOB);
  //
  //    // create an key object for the migrated key
  //    val destKey = TPMContext.context.createRsaKeyObject(TcTssConstants.TSS_KEY_SIZE_2048
  //      | TcTssConstants.TSS_KEY_TYPE_BIND | TcTssConstants.TSS_KEY_MIGRATABLE | TcTssConstants.TSS_KEY_NO_AUTHORIZATION);
  //
  //    // convert the migration blob to create a normal wrapped key
  //    destKey.convertMigrationBlob(pubKey, random, migratedData);
  //
  //    println(out(0).toHexString());
  //    // create encdata object and test decrption
  //
  //    destKey
  //    //maybe an TcBlobData has to be used for transporting to destination
  //    // this key is used at the destination to encrypt data
  //  }
  //
  /*
   * dont know if this is needed!
   */
  def loadMigrationBlog(publicKey: TcIRsaKey, random: TcBlobData, migrationBlob: TcBlobData): TcIRsaKey = {
    val encKey = TPMContext.context.createRsaKeyObject(TcTssConstants.TSS_KEY_SIZE_2048
      | TcTssConstants.TSS_KEY_TYPE_BIND | TcTssConstants.TSS_KEY_MIGRATABLE | TcTssConstants.TSS_KEY_AUTHORIZATION);
    encKey.convertMigrationBlob(publicKey, random, migrationBlob)
    TPMPolicy.keyUsgPolicy.assignToObject(encKey)
    //TPMContext.context.loadKeyByBlob(srk_, migrationBlob);
    encKey
  }
  def exportPublicKey(aKey: TpmAbstractKey, filename: String) {
    exportPublicKey(aKey.getKey(), filename)
  }
  def exportPublicKey(aKey: TcIRsaKey, filename: String = "/tmp/key.pub") {
    val file = new File(filename);
    val foStream = new FileOutputStream(file);
    val oStream = new ByteArrayOutputStream();
    val bs = aKey.getPubKey().asByteArray()
    //Writes a byte to the byte array output stream.
    oStream.write(bs);
    oStream.writeTo(foStream);
    TPMDebugger.log("Key written to the file " + filename);
    foStream.close();
  }
  def importPublicKey(filename: String): TcBlobData = {
    val file = new File(filename);
    val fiStream = new FileInputStream(file);

    val length = file.length();
    TPMDebugger.log("Reading file " + filename + " [" + length + "] Bytes")
    val data = new Array[Byte](length.asInstanceOf[Int])
    fiStream.read(data);
    fiStream.close();
    TcBlobData.newByteArray(data)
  }
  def getSRK() = {
    TPMDebugger.log("getting srk")
    TPMDebugger.log(srk_)
    srk_
  }
  def getNewUuid(prefix: Int = 0): TcTssUuid = {
    val keyUuid = new TcTssUuid().init(prefix, 0, 0, 0.asInstanceOf[Short], 0.asInstanceOf[Short], TPMContext.context.getTpmObject().getRandom(6).asShortArray());
    keyUuid
  }
  def createRsaKeyObject(encKeyPublicPart: TcBlobData) = {
    val tmpKey = createEmptyRsaKeyObject()
    tmpKey.setAttribData(TcTssConstants.TSS_TSPATTRIB_KEY_BLOB, TcTssConstants.TSS_TSPATTRIB_KEYBLOB_PUBLIC_KEY, encKeyPublicPart)
    tmpKey
  }
  def createEmptyRsaKeyObject() = {
    TPMContext.context.createRsaKeyObject(TcTssConstants.TSS_KEY_SIZE_2048
      | TcTssConstants.TSS_KEY_TYPE_BIND | TcTssConstants.TSS_KEY_MIGRATABLE | TcTssConstants.TSS_KEY_AUTHORIZATION);
  }
  def createBindingKey() = {
    val bindingKey = TpmBindingKey();
    val uuid = bindingKey.keyUuid.toStringNoPrefix();
    bindingKeyDb += (bindingKey.keyUuid.toStringNoPrefix() -> bindingKey)
    uuid
  }
  def getBindingKey(uuid: String) = {
    bindingKeyDb(uuid)
  }

}