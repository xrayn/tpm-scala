package net.ra23.tpm.config

import scala.collection.mutable._;

object TPMConfiguration {
  var myMap = Map.empty[String, String];
  val default =
    <config>
      <keyManager>
        <tpmPassword>12345</tpmPassword>
        <srkPassword>12345</srkPassword>
        <keyPassword>keyPwd</keyPassword>
        <pwdEncoding>UTF-16LE</pwdEncoding>
      </keyManager>
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
  }
  def fromXmlFile(filename: String) = {
    try fromXML(scala.xml.XML.load(filename)) 
    catch {
      case e: java.io.FileNotFoundException => fromXML(default)
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
  val partialDHKey = scala.math.abs(random.nextLong());
  var mac="";
  //println(fromXML(atest))

  //
  //println(fromXmlFile("/tmp/config.xml"))
}