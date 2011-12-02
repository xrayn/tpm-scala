package net.ra23.tpm.config

import scala.collection.mutable._;
import net.ra23.tpm.debugger._;

object TPMConfiguration {
  var myMap = Map.empty[String, String];
  val default =
    <config>
      <keyManager>
        <tpmPassword>12345</tpmPassword>
        <srkPassword>12345</srkPassword>
        <keyPassword>keyPwd</keyPassword>
        <pwdEncoding>UTF-16LE</pwdEncoding>
		<signingKeyPath>/tmp/</signingKeyPath>
      </keyManager>
	  <general>
		 <networkInterface>eth0</networkInterface>
		 <kernelCommDeviceReader>/dev/mcom</kernelCommDeviceReader>
		  <kernelCommDeviceWriter>/dev/mcom</kernelCommDeviceWriter>
		 <debugLogFile>/tmp/client1.log</debugLogFile>
		 <measurementLogFile>/tmp/measurement.log</measurementLogFile>
		 <daemonMode>0</daemonMode>
		  <autoMode>0</autoMode>
	</general>
    </config>
  def fromXML(node: scala.xml.Node) = {
    val trimmedNode = scala.xml.Utility.trim(node);
    trimmedNode \\ "keyManager" map {
      keyManagerNode =>
        {
          keyManagerNode.child map {
            configNode =>
              myMap(configNode.label) = configNode.text
          }
        }
    }
    trimmedNode \\ "general" map {
      keyManagerNode =>
        {
          keyManagerNode.child map {
            configNode =>
              myMap(configNode.label) = configNode.text
          }
        }
    }
  }
  def fromXmlFile(filename: String) = {
    try fromXML(scala.xml.XML.load(filename))
    catch {
      case e: java.io.FileNotFoundException => {
        TPMDebugger.log("File ["+filename+"] not found, loading default");
        fromXML(default)
      }
    }
    this
  }
  def get(key: String) = {
    myMap(key).toString()
  }
  override def toString() = {
    myMap.toString();
  }
  def toXmlFile(filename: String) {
    println("Writing default xml to file: " + filename)
    scala.xml.XML.saveFull(filename, default, "UTF-8", true, null);
  }
  val random = new scala.util.Random();
  var partialDHKey = scala.math.abs(random.nextLong());
  var mac = "";
  var aesKey = ""
  var tmqSplitSize = 512;
}