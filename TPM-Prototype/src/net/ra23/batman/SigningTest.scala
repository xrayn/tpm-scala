package net.ra23.batman

import net.ra23.tpm.sign;
import net.ra23.tpm.sign.TPMSigning
import net.ra23.tpm.config._;
import scala.io._;
import java.io._;
import net.ra23.tpm.base._;
import net.ra23.tpm.context._;
import java.net._;

object SigningTest {

  def main(args: Array[String]): Unit = {
    val baseDir = new File("").getAbsolutePath();
    val configfile = baseDir + "/etc/" + args(0);
    TPMConfiguration.fromXmlFile(configfile)
    val localNetworkInterface = NetworkInterface.getByName(TPMConfiguration.get("networkInterface"));
    val localMacAddress = localNetworkInterface.getHardwareAddress.toList.map(b => String.format("%02x", b.asInstanceOf[AnyRef])).mkString(":")
    TPMConfiguration.mac = localMacAddress
    TPMContext.context.connect();
    TPMSigning.measure();
  }

}