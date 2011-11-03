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
  var start = 0L
  def measure(message: Any, level: String = "", as: String = "") {
    //val date = new Date;
    level match {
      case "startup" => {
        start = System.nanoTime();
        Logger.debug("STARTTIME," + start.toString());
      }
      case "start" => {
        start = System.nanoTime();
        Logger.debug(message + ",START," + start.toString() + ",0," + as.toUpperCase());
      }
      case "end" => {
        val end = System.nanoTime();
        Logger.debug(message + ",END," + end.toString() + "," + ((end - start) / 1000) + "," + as.toUpperCase());
        start = 0L
      }
      case _ => Logger.info(message)
    }

  }
  def setFile(filename: String) {
    Logger.addAppender(new FileAppender(layout, filename));
  }
}