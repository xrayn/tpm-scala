package net.ra23.tpm.debugger
import org.apache.log4j.PropertyConfigurator;

object TPMDebugger {
 val Logger = org.apache.log4j.Logger.getLogger("TPM");
  Logger.setLevel(org.apache.log4j.Level.DEBUG)
  Logger.info("Logging enabled.");
  
  def log(message: Any, level: String = "info" ) {
    level match {
    	case "info"  => Logger.info(message)
    	case _ => Logger.info(message)
    }
    
  }
}