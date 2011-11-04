package net.ra23.batman.messages.fragmented;
import net.ra23.tpm.config._;
import net.ra23.batman.communication._;
import java.util.{ Date, Locale }
import java.text.DateFormat
import java.text.DateFormat._
import net.ra23.tpm.config;
import net.ra23.batman.communication._;
import scala.collection.mutable.Map;

case class FragmentedMessage(msg: String) {
  val content = msg.split("::")
  val state = content(0).replace("f","")
  val typ = content(1)
  val mac = content(2)
  val packet_number = content(3)
  val packet_max = content(4)
  val packet_meta = state + "f::" + typ + "::" + mac
  /*
   * create an array to hold the packet payloads
   */
  var payload = packet_number + "::" + packet_max + "::"
  payload += (if (content(5).endsWith("\n")) content(5).substring(0, content(5).length() - 1) else content(5))
  if (content.isDefinedAt(6)) {
    val propably_sml = if (content(6).endsWith("\n")) content(6).substring(0, content(6).length() - 1) else content(6)
    payload += "::"+propably_sml
  }
  def getPayload(): String = {
    payload
  }
}