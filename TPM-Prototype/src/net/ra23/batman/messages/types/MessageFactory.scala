package net.ra23.batman.messages.types

import net.ra23.tpm.debugger._;

object MessageFactory {
  def apply(message: String): BasicMessage = {
    message match {
      case msg: String if msg.startsWith("01::") => TmcMessage(msg)
      case msg: String if msg.startsWith("02::") => TmqMessage(msg)
      case msg: String if msg.startsWith("03::") => TmdMessage(msg)
      case msg: String => TPMDebugger.log(getClass().getSimpleName() + ": error while producing message: [" + msg + "]", "debug"); null
      case _ => TPMDebugger.log(getClass().getSimpleName() + ": undefined message format while parsing message."); null
    }
  }
}
