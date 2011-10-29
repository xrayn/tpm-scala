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

object TPMSigning {
  val tpm = TPM

  // is later only visible in getCertifiedKey
  val certifyKey = TpmSigningKey()

  def test() = {
    val foo = new TpmSigningKey;

    println(verifyCertifiedNonce(getQuote(), TPMKeymanager.createRsaKeyObject(TPMKeymanager.importPublicKey("/tmp/key.pub"))))
    //println(verifyCertifiedNonce(getQuote(), certifyKey))
  }

  def getQuote(): TcTssValidation = {
    val srk = TPMKeymanager.getSRK()
    val validationInput = new TcTssValidation();
    val nonce = TcBlobData.newByteArray(new Array[Byte](20));
    validationInput.setExternalData(nonce);

    val pcrComp: TcIPcrComposite = TPMContext.context.createPcrCompositeObject(TcTssConstants.TSS_PCRS_STRUCT_INFO);
    pcrComp.selectPcrIndex(1);
    pcrComp.selectPcrIndex(10);
    val result = TPMContext.context.getTpmObject.quote(certifyKey.getKey(), pcrComp, null)

    val dataToValidate = srk.certifyKey(certifyKey.getKey, result);
    //dataToValidate
    println(result.toString())
    println(dataToValidate.toString())
    dataToValidate
    
  }

  def verifyCertifiedNonce(dataToValidate: TcTssValidation, certifyKey: TcIRsaKey): Boolean = {

    val pubKey = new TcTpmPubkey(certifyKey.getPubKey());
    val pubKeyAsBlob = pubKey.getPubKey().getKey();
    val pubKeyDigest = pubKeyAsBlob.sha1();

    val plainData = dataToValidate.getData();
    val certifiedData = new TcTpmCertifyInfo(plainData);
    val certifiedDataPubKeyDigest = certifiedData.getPubKeyDigest().getDigest();
    println(pubKeyDigest.toHexStringNoWrap())
    println(certifiedDataPubKeyDigest.toHexStringNoWrap())
    //    //    // load the pubkey 
    //    //    val srk = TPMKeymanager.getSRK()
    //    //
    //    val plainData: TcBlobData = dataToValidate.getData();
    //    //
    //    val certifiedData = new TcTpmCertifyInfo(plainData);
    //    //scertifiedData.
    //    //
    //    //    // get pubkey of certified data
    //    //    //val certifiedDataPubKeyDigest: TcBlobData = certifiedData.getPubKeyDigest().getDigest();
    //    //
    //
    //    val sig: Signature = Signature.getInstance("SHA1withRSA");
    //    //    //val foo = TPMContext.context.getTpmObject.checkMaintenancePubKey(certifyKey, dataToValidate)
    //    //    //certifyKey.
    //
    //    sig.initVerify(TcCrypto.pubTpmKeyToJava(new TcTpmPubkey(certifyKey.getPubKey())));
    //    //sig.initVerify()
    //    sig.verify(certifyKey.getPubKey().asByteArray())
    //    sig.update(dataToValidate.getData().asByteArray());
    //    val valid: Boolean = sig.verify(dataToValidate.getData().asByteArray())
    //    valid
    //    //println(certifiedData.getPubKeyDigest().getDigest().toHexStringNoWrap());
    pubKeyDigest == certifiedDataPubKeyDigest
  }

}