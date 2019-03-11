package basics

import org.slf4j.LoggerFactory

class YourUsualClass {
  companion object {
    private val log = LoggerFactory.getLogger("ktintro.companion")

    @JvmStatic
    val abc = "In java (and kotlin) you get this as YourUsualClass.abc"

    @JvmStatic
    fun main(args: Array<String>) {
      YourUsualClass().hello("World")
      println(YourUsualClass.abc)
    }
  }

  // Can't use JvmStatic here

  fun hello(name: String) {
    log.info("Hello $name!")
  }
}

open class CompanionObjectInheritance(val x: Int) {
  companion object : CompanionObjectInheritance(10)

  fun printX() {
    println("x = $x")
  }
}

fun main(args: Array<String>) {
  CompanionObjectInheritance.printX()
  CompanionObjectInheritance(11).printX()
}