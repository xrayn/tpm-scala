import iaik.tc.tss.api.tspi.TcIRsaKey;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.api.tspi.TcIPolicy;
import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.structs.tsp.TcTssUuid;
import iaik.tc.tss.api.structs.tsp.TcUuidFactory;
import net.ra23.tpm.config._;
import net.ra23.tpm.context._;
import net.ra23.tpm.crypt._;

abstract class TpmAbstractKey {
  /*
   * configuration
   */
  val config = TPMConfiguration.fromXmlFile("/tmp/config.xml")
  val srkSecret: TcBlobData = TcBlobData.newString(TPMConfiguration.get("srkPassword"), false, TPMConfiguration.get("pwdEncoding"));
  val tpmSecret: TcBlobData = TcBlobData.newString(TPMConfiguration.get("tpmPassword"), false, TPMConfiguration.get("pwdEncoding"));
  val keySecret: TcBlobData = TcBlobData.newString(TPMConfiguration.get("keyPassword"), false, TPMConfiguration.get("pwdEncoding"));
  val srk = TPMContext.context.getKeyByUuid(TcTssConstants.TSS_PS_TYPE_SYSTEM,
    TcUuidFactory.getInstance().getUuidSRK());

  /*
   * policies
   */
  val srkPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
  srkPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN, srkSecret);
  srkPolicy.assignToObject(srk);

  val keyMigPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_MIGRATION);
  val keyUsgPolicy: TcIPolicy = TPMContext.context.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);

  /*
   * size definition 
   */
  val keySize = TcTssConstants.TSS_KEY_SIZE_2048;

  /*
   * abstract keytype definition is set in concrete class
   */

  val keyType: Long
  val migrateableType: Long

  /*
   * create and load the key into tpm
   */
  val key: TcIRsaKey = TPMContext.context.createRsaKeyObject(keySize | keyType | migrateableType)
  applyPolicies(key)
  key.createKey(srk, null)
  key.loadKey(srk)

  /*
   * generate a new uuid (for registering later)
   */
  var keyUuid = getNewUuid(57005)

  val publicKey: TcIRsaKey = null
  def applyPolicies(key: TcIRsaKey) = {
    keyMigPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN, keySecret);
    keyUsgPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN, keySecret);
    keyMigPolicy.assignToObject(key);
    keyUsgPolicy.assignToObject(key);
  }

  protected def getNewUuid(prefix: Int = 0): TcTssUuid = {
    val keyUuid = new TcTssUuid().init(prefix, 0, 0, 0.asInstanceOf[Short], 0.asInstanceOf[Short], TPMContext.context.getTpmObject().getRandom(6).asShortArray());
    keyUuid
  }

  def getPublicKey() {
    publicKey.getPubKey()
  }
  def getKey() = {
    key
  }
  override def toString() = {
    "Key:\n" +
      "Public: " + key.getPubKey().toHexStringNoWrap() + "\n" +
      "Unique: " + keyUuid.toStringNoPrefix()
  }

}