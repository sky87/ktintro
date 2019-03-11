package basics

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.util.concurrent.atomic.AtomicInteger

object ChannelsBasicExample {
  @JvmStatic
  fun main(args: Array<String>) {
    val ch = Channel<Int>()

    repeat(2) {
      GlobalScope.launch {
        println("Worker $it start")
        for (i in ch) {
          println("Worker $it took job $i")
        }
        println("Worker $it end")
      }
    }

    runBlocking {
      for (i in 0 .. 10) {
        ch.send(i)
      }
      ch.close() // This breaks the loop of coroutines suspended on the channel
    }
  }
}

@ExperimentalCoroutinesApi
object Pipelining {
  @JvmStatic
  fun main(args: Array<String>) = runBlocking {
    var counter = 0
    val N = 10
    val ch = produce {
      for (i in 0 .. N) {
        launch(Dispatchers.Default) {
          // do some IO that takes a long time like, an http request, in parallel
          delay(100)
          send(i)
        }
      }
    }.filter {
      counter += it
      false
    }

    for (ignore in ch);
    println("Counter: $counter == (${(N + 1)*N/2})")
  }
}