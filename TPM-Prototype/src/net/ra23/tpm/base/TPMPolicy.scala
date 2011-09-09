package net.ra23.tpm.base
import iaik.tc.tss.api.tspi.TcIPolicy;
import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.api.tspi.TcIRsaKey;
import iaik.tc.tss.api.tspi.TcIAuthObject;



import net.ra23.tpm.config._;
import net.ra23.tpm.context._;
import net.ra23.tpm.crypt._;

object TPMPolicy {
  val srkSecret: TcBlobData = TcBlobData.newString(TPMConfiguration.get("srkPassword"), false, TPMConfiguration.get("pwdEncoding"));
  val tpmSecret: TcBlobData = TcBlobData.newString(TPMConfiguration.get("tpmPassword"), false, TPMConfiguration.get("pwdEncoding"));
  val keySecret: TcBlobData = TcBlobData.newString(TPMConfiguration.get("keyPassword"), false, TPMConfiguration.get("pwdEncoding"));

  // set SRK policy
  val srkPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
  srkPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN, srkSecret);
  // setup TPM policy
  val tpmPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
  tpmPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN, tpmSecret);
  
    // create a key usage policy for this key
  val keyUsgPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
  TPMPolicy.keyUsgPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN, keySecret);
    //create a key migration policy for this key
  val keyMigPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_MIGRATION);
  TPMPolicy.keyMigPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN, keySecret);
  
  def applyPolicy(policy: TcIPolicy, obj: TcIAuthObject) {
    policy.assignToObject(obj)
  }
}