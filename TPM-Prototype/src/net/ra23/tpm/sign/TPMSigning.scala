package net.ra23.tpm.sign

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
import net.ra23.tpm.base._;
import net.ra23.tpm.context._;
import net.ra23.tpm.crypt._;
import net.ra23.tpm.debugger._;
import iaik.tc.tss.api.structs.tpm._;
import java.security.Signature;
import net.ra23.tpm.validate.TPMValidation;
import org.apache.commons.codec.binary.Base64;

class TPMSigning {
  
}
object TPMSigning {
  val tpm = TPM

  // is later only visible in getCertifiedKey
  val certifyKey = TpmSigningKey()
  //val certifyKey2 = TpmTestKey();
  def test() = {
    val foo = new TpmSigningKey;

    println(verifyCertifiedNonce(getQuoteBase64(), TPMKeymanager.createRsaKeyObject(TPMKeymanager.importPublicKey("/tmp/key.pub"))))
    //println(verifyCertifiedNonce(getQuote(), certifyKey))
  }
  def getQuoteBase64(): String =  {
     val dataToValidate = getQuote();
      val baos = new ByteArrayOutputStream();
      val resultObject = new ObjectOutputStream(baos)
      resultObject.writeObject(new TPMValidation(dataToValidate))
      resultObject.close
      new Base64().encodeAsString(baos.toByteArray());
  }
  
  def getQuote(): TcTssValidation = {
    val srk = TPMKeymanager.getSRK()
    val validationInput = new TcTssValidation();
    val nonceByteArray = Array[Byte](0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F, 0x50, 0x51, 0x52, 0x53, 0x54)

    val nonce = TcBlobData.newByteArray(nonceByteArray);
    validationInput.setExternalData(nonce);
    //println(validationInput.getExternalData().toStringASCII())

    val pcrComp: TcIPcrComposite = TPMContext.context.createPcrCompositeObject(TcTssConstants.TSS_PCRS_STRUCT_INFO);
    pcrComp.selectPcrIndex(1);
    pcrComp.selectPcrIndex(10);
    val result = TPMContext.context.getTpmObject.quote(certifyKey.getKey(), pcrComp, null)
    val dataToValidate = srk.certifyKey(certifyKey.getKey, result);
    dataToValidate
  }

  def verifyCertifiedNonce(dataToValidateBase64: String, certifyKey: TcIRsaKey): Boolean = {
    val data =   new Base64().decode(dataToValidateBase64);
    val bais = new ByteArrayInputStream(data)
    val in = new ObjectInputStream(bais);    
    val dataToValidate = in.readObject().asInstanceOf[TPMValidation];
    verifyCertifiedNonce(dataToValidate.getAsTcTssValidation(), certifyKey)
  }
  
  def verifyCertifiedNonce(dataToValidate: TcTssValidation, certifyKey: TcIRsaKey): Boolean = {
    val pubKey = new TcTpmPubkey(certifyKey.getPubKey());
    val pubKeyAsBlob = pubKey.getPubKey().getKey();
    val pubKeyDigest = pubKeyAsBlob.sha1();
    val plaindata = dataToValidate.getData()
    val certifiedData = new TcTpmCertifyInfo(plaindata);
    val certifiedDataPubKeyDigest = certifiedData.getPubKeyDigest().getDigest();
    pubKeyDigest == certifiedDataPubKeyDigest

  }

}