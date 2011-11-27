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
import net.ra23.tpm.config._;

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
  TPMDebugger.log("SRK Policy applied to srk", "debug")
  TPMPolicy.applyPolicy(TPMPolicy.tpmPolicy, tpm)
  TPMDebugger.log("SRK Policy applied to tpm", "debug")

  TPMDebugger.log("Loading signing key ... ", "debug")
  val signingKey = loadSigningKey()

  private def loadSigningKey(): TcIRsaKey = {
    val keyfile = TPMConfiguration.get("signingKeyPath") + TPMConfiguration.mac + ".key"
    var key: Option[TcIRsaKey] = None
    if (!new File(keyfile).isFile()) {
      exportSigningKey(keyfile)
      key = Some(TPMContext.context.loadKeyByBlob(getSRK(), importPublicKey(keyfile).get));
    } else {
      key = Some(TPMContext.context.loadKeyByBlob(getSRK(), importPublicKey(keyfile).get));
    }
    TPMPolicy.applyPolicy(TPMPolicy.keyUsgPolicy, key.get)
    TPMPolicy.applyPolicy(TPMPolicy.keyMigPolicy, key.get)
    key.get
  }

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
    try {
      //println("key is [" + aKey.getPubKey().toHexStringNoWrap() + "]")
      val bs = aKey.getPubKey().asByteArray()
      //Writes a byte to the byte array output stream.
      oStream.write(bs);
      oStream.writeTo(foStream);
      TPMDebugger.log("Key written to the file " + filename);
    } catch {
      case e: Exception => println("srk pubkey could not be exported"); e.printStackTrace();

    } finally {
      foStream.close();
    }

  }
  def importPublicKey(filename: String): Option[TcBlobData] = {
    val file = new File(filename);
    var fiStream: Option[FileInputStream] = None;
    try {
      fiStream = Some(new FileInputStream(file));

      val length = file.length()
      TPMDebugger.log("Reading file " + filename + " [" + length + "] Bytes", "debug")
      val data = new Array[Byte](length.asInstanceOf[Int])
      fiStream.get.read(data);
      fiStream.get.close();
      Some(TcBlobData.newByteArray(data))
    } catch {
      case e: FileNotFoundException =>
        TPMDebugger.log("There is no file [" + "/tmp/bc:ae:c5:2a:90:c2.pub" + "]", "info");
        None
    }

  }

  def getSRK() = {
    TPMDebugger.log("getting srk", "debug")
    TPMDebugger.log(srk_, "debug")
    srk_
  }
  def getNewUuid(prefix: Int = 0): TcTssUuid = {
    val keyUuid = new TcTssUuid().init(prefix, 0, 0, 0.asInstanceOf[Short], 0.asInstanceOf[Short], TPMContext.context.getTpmObject().getRandom(6).asShortArray());
    keyUuid
  }
  def createRsaKeyObject(encKeyPublicPart: Option[TcBlobData]): Option[TcIRsaKey] = {
    val tmpKey = createEmptyRsaKeyObject()
    tmpKey.setAttribData(TcTssConstants.TSS_TSPATTRIB_KEY_BLOB, TcTssConstants.TSS_TSPATTRIB_KEYBLOB_PUBLIC_KEY, encKeyPublicPart.getOrElse(return None))
    Some(tmpKey)
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
  def exportSigningKey(filename: String = "/tmp/signing.key") {
    val signingKey = TpmSigningKey();
    val file = new File(filename);
    val foStream = new FileOutputStream(file);
    val oStream = new ByteArrayOutputStream();
    //println("key is [" + signingKey.getKey()..toHexStringNoWrap() + "]")
    val bs = getKeyBlobData(signingKey.getKey()).asByteArray()
    //Writes a byte to the byte array output stream.
    oStream.write(bs);
    oStream.writeTo(foStream);
    TPMDebugger.log("Key written to the file " + filename);
    exportPublicKey(signingKey, filename+".pub")
  }
  def getKeyBlobData(someKey: TcIRsaKey): TcBlobData = {
    val bd: TcBlobData = someKey.getAttribData(TcTssConstants.TSS_TSPATTRIB_KEY_BLOB,
      TcTssConstants.TSS_TSPATTRIB_KEYBLOB_BLOB);
    TPMContext.context.loadKeyByBlob(srk_, bd)
    bd
  }
}