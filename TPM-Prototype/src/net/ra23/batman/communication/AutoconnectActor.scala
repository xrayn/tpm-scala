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
    // do not spam the client only send packets to a single node every 500 ms
    if (state2Tracker.isDefinedAt(mac) && (currentTime - state2Tracker(mac) > 500*1000*1000)) {
      state2Tracker(mac) = currentTime
      true
    } else if (!state2Tracker.isDefinedAt(mac)) {
      state2Tracker+= mac -> currentTime
      true
    } else {
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
              for (payload <- PayloadHelper.splitPayload(TPMSigning.getQuoteBase64() + "::CLIENT_SML_HASH", TPMConfiguration.tmqSplitSize))
                result = Some(Unicast("02::" + mac + "::02f::c::" + TPMConfiguration.mac + "::" + payload)) :: result
              DeviceWriterActor ! result.reverse
            }
          }
        }
        Thread.sleep(50);
      }
    }
  }
  start
}