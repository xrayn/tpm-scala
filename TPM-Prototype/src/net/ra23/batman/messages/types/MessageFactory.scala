package net.ra23.batman.messages.types

import net.ra23.tpm.debugger._;

object MessageFactory {
  def apply(message: String): BasicMessage = {
    message match {
      case msg: String if msg.startsWith("01::") && !msg.isDefinedAt(3) => TPMDebugger.log(getClass().getSimpleName() + ": message: [" + msg + "] has no attached DH_KEY. Fired from send_skb_packet, look into it!", "debug"); null
      case msg: String if msg.startsWith("01::") && msg.isDefinedAt(3) => TmcMessage(msg)
      case msg: String if msg.startsWith("02::") => TmqMessage(msg)
      case msg: String if msg.startsWith("03::") => TmdMessage(msg)
      case msg: String => TPMDebugger.log(getClass().getSimpleName() + ": error while producing message: [" + msg + "]", "debug"); null
      case _ => TPMDebugger.log(getClass().getSimpleName() + ": undefined message format while parsing message."); null
    }
  }
}
