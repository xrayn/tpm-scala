package net.ra23.helper

import net.ra23.tpm.debugger.TPMDebugger;
import scala.util.matching.Regex;

object PayloadHelper {

  def splitPayload(payload: String, size: Int): List[String] = {
    var splitsize = size;
    val splitted = payload.split("""(?<=\G.{""" + splitsize + """})""").toList;
    // check if :: is beginning/end/cutted. If decrement/increment splitsize and split again!
    // maybe cutted does not matter!
    var result = List[String]()
    for (id <- 0 until splitted.length) {
      result = ((id + 1) + "::" + (splitted.length) + "::" + splitted(id)) :: result

    }
    result.reverse
  }
  def mergePayload(payloads: List[String]): String = {

    for (payload <- payloads) TPMDebugger.log(getClass().getSimpleName() + ":merging -> [" + payload + "]", "debug");
    var result = List[String]()
    // for very small sizes it is possible for a tmq packet to be cutted in between the last ::
    // or :: is at the end of the message and gets cutted. Handle this!
    // not sure if cutting in between :: is handled false! 
    val sorted = payloads.sort((e1, e2) => (e1.split("::")(0).toInt < e2.split("::")(0).toInt))
    for (payload <- sorted) {
      result = payload.split("::")(2) :: result;
      if (payload.split("::").isDefinedAt(3))
        result = "::" + payload.split("::")(3) :: result;
    }
    result.reverse.foldLeft("")((x, y) => x + y)
  }

}