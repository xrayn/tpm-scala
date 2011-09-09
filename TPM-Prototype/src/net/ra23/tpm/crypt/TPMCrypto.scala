package net.ra23.tpm.crypt;

import iaik.tc.tss.api.tspi.TcIRsaKey;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.impl.csp.TcCrypto;
import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.impl.csp.TcBasicCrypto;
import iaik.tc.tss.api.tspi.TcIEncData;
import iaik.tc.tss.api.tspi.TcIPolicy;

import net.ra23.tpm.config._;
import net.ra23.tpm.context._;
import net.ra23.tpm.base._;


object TPMCrypto {
  /*
   * encrypt and decrypt should be in another module!
   */
  def encrypt(encKeyPublicPart: TcBlobData, plaintext: String): TcIEncData = {
    /*
     * a temporary empty key is needed, which gets the public part injected
     */
    encrypt(TPMKeymanager.createRsaKeyObject(encKeyPublicPart), plaintext)
  }

  def encrypt(encKey: TcIRsaKey, plaintext: String): TcIEncData = {
    val encData = TPMContext.context.createEncDataObject(TcTssConstants.TSS_ENCDATA_BIND);

    // bind
    val rawData = TcBlobData.newString(plaintext);

    encData.bind(encKey, rawData);
    val boundData = encData.getAttribData(TcTssConstants.TSS_TSPATTRIB_ENCDATA_BLOB,
      TcTssConstants.TSS_TSPATTRIB_ENCDATABLOB_BLOB);
    println(boundData.toHexStringNoWrap())
    encData
    //maybe has to be TcIBlobData for transportation

  }
  
  def decrypt (encData: TcIEncData, encKey: TpmAbstractKey) {
    decrypt(encData, encKey.getKey())
  }
  
  def decrypt(encData: TcIEncData, encKey: TcIRsaKey) {
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