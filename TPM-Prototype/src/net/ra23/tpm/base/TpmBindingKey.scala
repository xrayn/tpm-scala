package net.ra23.tpm.base

import iaik.tc.tss.api.tspi.TcIRsaKey;

import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.tspi.TcIPolicy;
import iaik.tc.tss.api.structs.tsp.TcUuidFactory;

case class TpmBindingKey() extends TpmAbstractKey {
  val keyType = TcTssConstants.TSS_KEY_TYPE_BIND;
  val migrateableType = TcTssConstants.TSS_KEY_NOT_MIGRATABLE

}