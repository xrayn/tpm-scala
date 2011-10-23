package net.ra23.batman.measurement

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j._;
import java.util.Date;

object MessageMeasurer {

  val layout = new SimpleLayout()
  val Logger = org.apache.log4j.Logger.getLogger("Measurement");
  Logger.setLevel(org.apache.log4j.Level.ALL)
  Logger.removeAllAppenders();
  Logger.info("Measurement enabled.");

  def measure(message: Any, level: String = "") {
    val date = new Date;
    level match {
      case "startup" => {
        Logger.debug("STARTTIME,"+date.getTime().toString());
      }
      case "start" => {
        Logger.debug(message+",START,"+date.getTime().toString());
      }
      case "end" => {
        Logger.debug(message+",END,"+date.getTime().toString());
      }
      case _ => Logger.info(message)
    }

  }
  def setFile(filename: String) {
    Logger.addAppender(new FileAppender(layout, filename));
  }
}