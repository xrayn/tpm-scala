package net.ra23.tpm.debugger
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j._;

object TPMDebugger {
  
  val layout = new SimpleLayout()
 val Logger = org.apache.log4j.Logger.getLogger("TPM");
  Logger.setLevel(org.apache.log4j.Level.ALL)
  Logger.removeAllAppenders();
  Logger.addAppender(new FileAppender(layout, "/tmp/test.log"));
  Logger.info("Logging enabled.");
  
  def log(message: Any, level: String = "info" ) {
    level match {
    	case "info"  => Logger.info(message)
    	case "debug"  => Logger.debug(message)
    	case _ => Logger.info(message)
    }
    
  }
}