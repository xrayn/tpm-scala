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
  var start = new Date().getTime();
  def measure() {
    //val date = new Date;
    val nodeList = ConnectionStorage.getMeasurementList();
    var sum=0;
    var message = "NODE "+((new Date().getTime()-start)).toInt+","
      nodeList.reverse.foreach(single => {message = message+""+single.tail(0)+","; sum = sum+single.tail(0).asInstanceOf[Int]})
      message=message+sum;
     Logger.debug(message.toString())
  }
  def setFile(filename: String) {
    Logger.addAppender(new FileAppender(layout, filename));
  }
  
}