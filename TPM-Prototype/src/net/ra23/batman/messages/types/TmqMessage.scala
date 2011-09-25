package net.ra23.batman.messages.types

case class TmqMessage(msg: String) extends BasicMessage(msg) {
  payload = payload + ", "+ (if (content(3).endsWith("\n")) content(3).substring(0, content(3).length() - 1) else content(3))
}