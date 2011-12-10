package net.ra23.batman.measurement

import net.ra23.batman._;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j._;
import java.util.Date;

object NodesMeasurer {

  val layout = new SimpleLayout()
  val Logger = org.apache.log4j.Logger.getLogger("NodesMeasurement");
  Logger.setLevel(org.apache.log4j.Level.ALL)
  Logger.removeAllAppenders();
  Logger.info("Nodes Measurement enabled.");
  var start = 0L;
  var lastmessage = ""
  def measure() {
    if (start==0) {
      // start measurement with firs apperance of a message (TMC normally)
      start=new Date().getTime()
    }
    //val date = new Date;
    val nodeList = ConnectionStorage.getMeasurementList();
    var sum = 0;
    var message = ""
    nodeList.reverse.foreach(single => {
      message = message + "" + single.tail(0) + ",";
      sum = sum + single.tail(0).asInstanceOf[Int]
    })
    message = message + sum;
    // only log if something changed!
    if (lastmessage != message) {
      lastmessage = message;
      Logger.debug("NODE "+((new Date().getTime()-start)).toInt+"," + message.toString())
    }
  }
  def setFile(filename: String) {
    Logger.addAppender(new FileAppender(layout, filename));
  }

}