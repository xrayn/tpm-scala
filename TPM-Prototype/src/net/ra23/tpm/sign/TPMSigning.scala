package net.ra23.tpm.sign

import java.math._;
import net.ra23.batman.measurement._;
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
import net.ra23.tpm.config._;
import iaik.tc.tss.api.structs.tpm._;
import java.security.Signature;
import net.ra23.tpm.validate.TPMValidation;
import org.apache.commons.codec.binary.Base64;
import net.ra23.batman.messages.fragmented._;

class TPMSigning {

}
object TPMSigning {
  val tpm = TPM
  def measure() {
    val fstream = new FileWriter("/tmp/quote_verify.csv");
    val out = new BufferedWriter(fstream);
    val akey = TPMKeymanager.createRsaKeyObject(TPMKeymanager.importPublicKey(TPMConfiguration.get("signingKeyPath") + TPMConfiguration.mac + ".key.pub"));
    for (i <- 1 to TPMConfiguration.get("measureQuote").toInt) {
      val result = singleGetQuote()
      val quoteTime = result._1
      val verifyTime = singleVerifyQuote(result._2, akey);
      out.write("QUOTE," + quoteTime + "\n")
      out.write("VERIFY," + verifyTime + "\n")
      out.flush();
    }
    out.close();
  }
  def singleGetQuote(): Tuple2[Long, String] = {
    val start = System.nanoTime();
    val result = TPMSigning.getQuoteBase64();
    println(result);
    val end = System.nanoTime();
    Tuple2(end - start, result);
  }
  def singleVerifyQuote(dataToValidateBase64: String, certifyKey: Option[TcIRsaKey], mac: String = ""): Long = {
    val start = System.nanoTime();
    val result = verifyCertifiedNonce(dataToValidateBase64, certifyKey);
    val end = System.nanoTime();
    println(result);
    end - start
  }
  def test() = {
    println(verifyCertifiedNonce(getQuoteBase64(), TPMKeymanager.createRsaKeyObject(TPMKeymanager.importPublicKey("/tmp/bc:ae:c5:2a:90:c2.key.pub"))))
  }
  def selfTest() {
    if (!verifyCertifiedNonce(getQuoteBase64(), TPMKeymanager.createRsaKeyObject(TPMKeymanager.importPublicKey(TPMConfiguration.get("signingKeyPath") + TPMConfiguration.mac + ".key.pub")))) {
      println("Signing selftest failed!");
      println("Most likely pubkey does not match private key=>")
      println("Delete private key [" + TPMConfiguration.get("signingKeyPath") + TPMConfiguration.mac + ".key] to regenerate both keys");
      System.exit(-1)
    }
  }
  def getQuoteBase64(mac: String = ""): String = {
    val dataToValidate = getQuote(mac);
    val baos = new ByteArrayOutputStream();
    val resultObject = new ObjectOutputStream(baos)
    resultObject.writeObject(new TPMValidation(dataToValidate))
    resultObject.close
    new Base64().encodeAsString(baos.toByteArray());
  }

  def getQuote(mac: String): TcTssValidation = {
    val start = System.nanoTime();
    val validationInput = new TcTssValidation();
    val nonceByteArray = Array[Byte](0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F, 0x50, 0x51, 0x52, 0x53, 0x54)
    val nonce = TcBlobData.newByteArray(nonceByteArray);
    validationInput.setExternalData(nonce);
    val pcrComp: TcIPcrComposite = TPMContext.context.createPcrCompositeObject(TcTssConstants.TSS_PCRS_STRUCT_INFO);
    pcrComp.selectPcrIndex(1);
    pcrComp.selectPcrIndex(10);
    val result = TPMContext.context.getTpmObject.quote(TPMKeymanager.signingKey, pcrComp, null)
    val end = System.nanoTime();
    if (mac != "")
    MessageMeasurer.measure(mac + "," + getClass().getSimpleName().toUpperCase() +","+((end-start)/1000)+",", "already_calculated", "TPM_QUOTE_CALCULATION_TIME");
    result
  }
  def verifyCertifiedNonce(dataToValidateBase64: String, certifyKey: Option[TcIRsaKey], mac: String = ""): Boolean = {
    
    val data = new Base64().decode(dataToValidateBase64);
    val bais = new ByteArrayInputStream(data)
    try {
      
      val in = new ObjectInputStream(bais);
      val dataToValidate = in.readObject().asInstanceOf[TPMValidation];
      val start = System.nanoTime();
      val res = verifyCertifiedNonce(dataToValidate.getAsTcTssValidation(), certifyKey)
      val end = System.nanoTime();
      if (mac != "")
      MessageMeasurer.measure(mac + "," + getClass().getSimpleName().toUpperCase() +","+((end-start)/1000)+",", "already_calculated", "TPM_QUOTE_VERIFICATION_TIME");
      res
    } catch {
      case e: java.io.StreamCorruptedException => {
        TPMDebugger.log(getClass().getSimpleName() + "Corrupted steam, just continue!.", "info");
        false
      }
      case e: Exception => {
        /**
         * if a packet in a fragmented sequence gets not received (for whatever reason) it is possible,
         * that this message gets invalid.
         * Lets assume the fragmented message is in two parts. If sequence #1 is not received, sequence #2 is inserted into
         * the fragmented message storage. Now a new message with sequence #1 is received.
         * The storage now assumes the message is complete merges it and passes it to this function.
         * When the decryption kicks in (decryptRsaEcbPkcs1Padding(...)) the invalid message can not be decrypted and a Exception
         * is thrown.
         *
         *  Currently this is the only part in the program where this can be recognized.
         *  (it would be far more complicated to validate the consistency of the message [e.g. introducing another number
         *  which identifies each message sequence list])
         *
         *  For the further handling it doesn't matter if the message is wrong by mistake or by intention (from an attacker)
         *  the verification process will just fail and the program will continue.
         *  To fix the sequence bug, we empty the FragmentedMessage Storage.
         *
         *  This only works if the next sequence message is already in the FramgentedStorage. (We will see how this works out).
         *
         *  @todo: Implement clean sequence handling!
         *
         */

        TPMDebugger.log(getClass().getSimpleName() + "Something went wrong! Handling message as invalid and continue.");
        TPMDebugger.log(getClass().getSimpleName() + "Maybe sequences get messed up cleaning the FragmentedMessageStorage for mac [" + mac + "]");
        FragmentedMessageStorage.cleanup(mac);
        false
      }
    }
  }

  /**
   * The validation data is encrypted with the private part of the signing key.
   * After the data is decrypted with the public signing key part, the hash of getData must be equal to
   * this decrypted data.
   */
  def verifyCertifiedNonce(dataToValidate: TcTssValidation, certifyKey: Option[TcIRsaKey]): Boolean = {
    val pubKey = new TcTpmPubkey(certifyKey.getOrElse(return false).getPubKey());
    //val plaindata = TcCrypto.decryptRsaEcbPkcs1Padding(pubKey, dataToValidate.getValidationData())
    val plainDataDigest = decryptValidationData(dataToValidate, pubKey);
    isEqual(dataToValidate, plainDataDigest);
  }
  /**
   * Only the last 20 bytes are returned, they contain the sha1 hash. The first 15 bytes must not be used
   * for verifying (fixed data?)
   */
  private def decryptValidationData(dataToValidate: TcTssValidation, pubKey: TcTpmPubkey): TcBlobData = {
    val decrypted = TcCrypto.decryptRsaEcbPkcs1Padding(pubKey, dataToValidate.getValidationData())
    val result = TcBlobData.newByteArray(decrypted.asByteArray(), 15, 20);
    result
  }
  private def isEqual(dataToValidate: TcTssValidation, plainDataDigest: TcBlobData): Boolean = {
    if (dataToValidate.getData().sha1() == plainDataDigest) {
      true
    } else {
      TPMDebugger.log(getClass().getSimpleName() + ": Digests are not equal");
      TPMDebugger.log(getClass().getSimpleName() + ": " + dataToValidate.getData().sha1().toHexStringNoWrap());
      TPMDebugger.log(getClass().getSimpleName() + ": " + plainDataDigest.toHexStringNoWrap());
      false
    }
  }
}