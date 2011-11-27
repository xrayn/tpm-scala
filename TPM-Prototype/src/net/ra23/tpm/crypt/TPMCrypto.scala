package net.ra23.tpm.crypt;

import iaik.tc.tss.api.tspi.TcIRsaKey;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.impl.csp.TcCrypto;
import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.impl.csp.TcBasicCrypto;
import iaik.tc.tss.api.tspi.TcIEncData;
import iaik.tc.tss.api.tspi.TcIPolicy;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.spec.MGF1ParameterSpec;
import iaik.tc.tss.impl.csp._;
import iaik.tc.tss.impl.java.tsp.TcRsaKey;
import iaik.tc.tss.api.structs.tpm.{ TcTpmBoundData, TcTpmPubkey, TcTpmStructVer };
import iaik.tc.tss.api.constants.tpm.TcTpmConstants;
import net.ra23.tpm.config._;
import net.ra23.tpm.context._;
import net.ra23.tpm.base._;
import net.ra23.tpm.debugger._;

import javax.crypto._;
import java.security._;

object TPMCrypto {

  def encrypt(encKey: TpmBindingKey, plainData: String): TcIEncData = {
    TPMDebugger.log("------ Enrypting STARTED  ---------");
    val pubKeyJava = encKey.getPublicJavaKey();
    val rsaCa = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
    val oaepSpec = new OAEPParameterSpec("SHA1", "MGF1", new MGF1ParameterSpec(
      "SHA1"), new PSource.PSpecified("TCPA".getBytes("ASCII")));
    rsaCa.init(Cipher.ENCRYPT_MODE, pubKeyJava, oaepSpec);
    val rawdata = TcBlobData.newString(plainData)
    /*
     * data has to be wrapped in TcTpmBoundData! 
     * so: add TPM_BOUND_DATA structure
     */
    val boundData = new TcTpmBoundData();
    boundData.setPayload(TcTpmConstants.TPM_PT_BIND);
    boundData.setPayloadData(rawdata);
    boundData.setVer(TcTpmStructVer.TPM_V1_1);

    val encData = TcBlobData.newByteArray(rsaCa.doFinal(boundData.getEncoded().asByteArray()));

    val encDataObject = TPMContext.context.createEncDataObject(TcTssConstants.TSS_ENCDATA_BIND);

    /*
     * following method can be called also.
     * what this function does, we do by hand above!
     *
     *val encKeyInternal = encKey.getKey.asInstanceOf[TcRsaKey];
     *val pubKey = new TcTpmPubkey(encKeyInternal.getAttribKeyBlob(TcTssConstants.TSS_TSPATTRIB_KEYBLOB_PUBLIC_KEY));
     *encDataObject.setAttribData(TcTssConstants.TSS_TSPATTRIB_ENCDATA_BLOB, TcTssConstants.TSS_TSPATTRIB_ENCDATABLOB_BLOB, iaik.tc.tss.impl.csp.TcCrypto.pubEncryptRsaOaepSha1Mgf1(pubKey, boundData.getEncoded()))
    */
    encDataObject.setAttribData(TcTssConstants.TSS_TSPATTRIB_ENCDATA_BLOB, TcTssConstants.TSS_TSPATTRIB_ENCDATABLOB_BLOB, encData)

    TPMDebugger.log("------ Enrypting FINISHED ---------");
    encDataObject
  }

  def encryptByTpm(encKey: TpmBindingKey, plainData: String): TcIEncData = {
    TPMDebugger.log("------ Enrypting TPM STARTED -----");
    val result = encrypt(encKey.getKey(), plainData)
    TPMDebugger.log("------ Enrypting FINISHED ---------");
    result

  }

  /*
   * encrypt and decrypt should be in another module!
   */
  protected def encrypt(encKeyPublicPart: TcBlobData, plaintext: String): TcIEncData = {
    /*
     * a temporary empty key is needed, which gets the public part injected
     */
    encrypt(TPMKeymanager.createRsaKeyObject(Some(encKeyPublicPart)).get, plaintext)
  }

  protected def encrypt(encKey: TcIRsaKey, plaintext: String): TcIEncData = {
    val encData = TPMContext.context.createEncDataObject(TcTssConstants.TSS_ENCDATA_BIND);
    // bind
    val rawData = TcBlobData.newString(plaintext);

    encData.bind(encKey, rawData);
    val boundData = encData.getAttribData(TcTssConstants.TSS_TSPATTRIB_ENCDATA_BLOB,
      TcTssConstants.TSS_TSPATTRIB_ENCDATABLOB_BLOB);
    println(boundData.toHexStringNoWrap())
    //println(encKey.getAttribData(TcTssConstants.TSS_TSPATTRIB_RSAKEY_INFO, ))
    encData
    //maybe has to be TcIBlobData for transportation

  }

  def decrypt(encData: TcIEncData, encKey: TpmBindingKey) {
    decrypt(encData, encKey.getKey())
  }

  protected def decrypt(encData: TcIEncData, encKey: TcIRsaKey) {
    /*
     * 
     * use our public key to decrypt the encrypted message from the destination.
     */

    /*
     * get this later on from the keymanager!
     */
    val keySecret: TcBlobData = TcBlobData.newString(TPMConfiguration.get("keyPassword"), false, TPMConfiguration.get("pwdEncoding"));
    val keyUsgPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
    keyUsgPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN, keySecret);

    keyUsgPolicy.assignToObject(encKey)
    //encKey.loadKey(pubKey)
    val unboundData = encData.unbind(encKey);
    println(unboundData.toString());
  }

}