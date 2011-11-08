package net.ra23.batman.messages.types

import net.ra23.tpm.debugger._;

object MessageFactory {
  def apply(message: String): Option[BasicMessage] = {
    try {
      message match {
        case msg: String if msg.startsWith("01::") => Some(TmcMessage(msg))
        //case msg: String if msg.startsWith("01::") => TPMDebugger.log(getClass().getSimpleName() + ": error while producing message: [" + msg + "]", "debug"); null;
        case msg: String if msg.startsWith("02::") => Some(TmqMessage(msg))

        case msg: String if msg.startsWith("03::") => Some(TmdMessage(msg))
        case msg: String => TPMDebugger.log(getClass().getSimpleName() + ": error while producing message: [" + msg + "]", "debug"); null
        case _ => TPMDebugger.log(getClass().getSimpleName() + ": undefined message format while parsing message."); null
      }
    } catch {
      case e: Exception =>
        {
          TPMDebugger.log(getClass().getSimpleName() + "Got exception while trying to produce a message. Message was [" + message + "]", "debug");
          TPMDebugger.log(getClass().getSimpleName() + "Exception: " + e.getStackTrace(), "debug");
        }
        None
    }
  }
}
