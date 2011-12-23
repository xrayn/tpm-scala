package net.ra23.batman.communication

import scala.actors.Actor
import scala.collection.mutable.Map;
import scala.actors.Actor._
import java.io._;
import net.ra23.tpm.debugger._;
import java.lang.ProcessBuilder;
import net.ra23.tpm.config._;
import net.ra23.batman.ConnectionStorage;
import net.ra23.helper.PayloadHelper;
import net.ra23.tpm.sign.TPMSigning;
import net.ra23.batman.measurement.NodesMeasurer;

object AutoconnectActor extends Actor {
  var count = 0;
  var run = true;
  var started = false;
  var startTime = 0L;
  var state2Tracker = Map[String, Long]();
  def act = loop {
    react {
      case msg: String if msg == "start" => {
        run = true;
        started = true;
        TPMDebugger.log(getClass().getSimpleName() + " start", "debug");
        startTime = System.nanoTime();
        startAutoConnect
      }
      case msg: String if msg == "restart" => {

        TPMDebugger.log(getClass().getSimpleName() + " restart", "debug");
        if (started) this ! "stop"
        this ! "start"
      }
      case msg: String if msg == "stop" => {
        count = 0;
        run = false;
        TPMDebugger.log(getClass().getSimpleName() + " stop", "debug");
      }
      case _ => println(getClass().getSimpleName() + "error in react");
    }
  }
  def shouldRun(mac: String, currentTime: Long): Boolean = {
    // do not spam the client only send packets to a single node every 1 s
    if (!state2Tracker.isDefinedAt(mac)) {
      //println("[" + mac + "] run ->[" + currentTime + "]" + " first")
      state2Tracker += mac -> currentTime
      true
    } else if (state2Tracker.isDefinedAt(mac) && (currentTime - state2Tracker(mac) > (500000000L))) {
      //println("[" + mac + "] run ->[" + currentTime + "]" + " delta [" + (currentTime - state2Tracker(mac)) + "]")
      state2Tracker(mac) = currentTime
      true
    } else {
      //println("[" + mac + "] no  ->[" + currentTime + "]" + " delta [" + (currentTime - state2Tracker(mac)) + "]")
      false
    }
  }
  def startAutoConnect = {
    actor {
      while (run) {
        count = count + 1;
        //TPMDebugger.log(getClass().getSimpleName() + ": Autoconnect cicle [" + count + "]", "debug");
        for (mac <- ConnectionStorage.getNotInState3()) {
          val currentTime = System.nanoTime();
          if (shouldRun(mac, currentTime)) {
            var result = List[Option[Unicast]]()
            val key = ConnectionStorage.getDhKey(mac);
            if (key != None) {
              val partialDhKey = key.get;
              for (payload <- PayloadHelper.splitPayload(TPMSigning.getQuoteBase64(mac) + "::CLIENT_SML_HASH", TPMConfiguration.tmqSplitSize))
                result = Some(Unicast("02::" + mac + "::02f::c::" + TPMConfiguration.mac + "::" + payload)) :: result
              println("message sent")
              DeviceWriterActor ! result.reverse
            } else {
              println("no key yet");
            }
          }
        }
        if ((System.nanoTime() - startTime > 3000000000L) && (ConnectionStorage.keyDb.size == (ConnectionStorage.keyDb.size - ConnectionStorage.getNotInState3().length)) && TPMConfiguration.get("autoMode") == "1") {
          println("All Nodes connected exit now! ["+((System.nanoTime() - startTime) / (1000*1000))+"] ms runtime");
          System.exit(0)
        }
        Thread.sleep(100);
      }
    }
  }
  start
}