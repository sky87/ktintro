package basics

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


object HowDoesItWork {
  @JvmStatic
  fun main(args: Array<String>) = runBlocking {
    example()
  }

  suspend fun example() {
    var counter = 0

    delay(100)

    counter++

    delay(200)

    println("Counter is $counter")
  }
}