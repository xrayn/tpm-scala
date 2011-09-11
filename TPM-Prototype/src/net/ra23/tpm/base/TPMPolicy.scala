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

  // set SRK policy
  val srkPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
  applyConfigSecretToPolicy(srkPolicy, "srkPassword")
  // setup TPM policy
  val tpmPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
  applyConfigSecretToPolicy(tpmPolicy, "tpmPassword")
  
    // create a key usage policy for this key
  val keyUsgPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
  applyConfigSecretToPolicy(keyUsgPolicy, "keyPassword")
    //create a key migration policy for this key
  val keyMigPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_MIGRATION);
  applyConfigSecretToPolicy(keyUsgPolicy, "keyPassword")
  
  /**
   * applies a Policy to an object. Its just a Wrapper for better handling.
   */
  def applyPolicy(policy: TcIPolicy, obj: TcIAuthObject) {
    policy.assignToObject(obj)
  }
  /**
   * read a config entry from the config object and set the password for the policy
   */
  private def applyConfigSecretToPolicy(policy: TcIPolicy, configEntry: String) {
    val secretAsBlob = TcBlobData.newString(TPMConfiguration.get(configEntry), false, TPMConfiguration.get("pwdEncoding"));
    policy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN, secretAsBlob);
  }
}