package net.ra23.batman

object Test {

  def main(args: Array[String]): Unit = {
    val foo = List(
        "3::acbv",
        "2::acbv",
        "7::acbv",
        "4::acbv",
        "5::acbv",
        "6::acbv",
        "1::acbv"
        )
    println(foo.sort((e1, e2) => (e1.split("::")(0).toInt < e2.split("::")(0).toInt)))
    
    
  }

}