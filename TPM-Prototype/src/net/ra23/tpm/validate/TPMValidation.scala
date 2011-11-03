package net.ra23.tpm.validate

import iaik.tc.tss.api.structs.tsp.TcTssValidation
import iaik.tc.tss.api.structs.common.TcBlobData
import iaik.tc.tss.api.structs.tsp.TcTssVersion

case class TPMValidation(version: String, data: Array[Byte], externalData: Array[Byte], validationData: Array[Byte]) {

  def this(version: TcTssVersion, data: TcBlobData, externalData: TcBlobData, validationData: TcBlobData) =
    this(version.toString(), data.asByteArray(), externalData.asByteArray(), validationData.asByteArray())
  def this(obj: TcTssValidation) = this(obj.getVersionInfo(), obj.getData(), obj.getExternalData(), obj.getValidationData());

  override def toString() = {
    "Version: " + version.toString() + "\n" +
      "Data: [" + data.length + "]" +
      "externalData: [" + externalData.length + "]" +
      "validationData: [" + validationData.length + "]" + "\n"
  }

  def getAsTcTssValidation(): TcTssValidation = {
    val tcv = new TcTssValidation();
    val version = new TcTssVersion()
    version.init(1, 1, 0, 0)
    tcv.setVersionInfo(version)
    tcv.setExternalData(TcBlobData.newByteArray(externalData))
    tcv.setData(TcBlobData.newByteArray(data))
    tcv.setValidationData(TcBlobData.newByteArray(validationData))
    tcv
  }
}