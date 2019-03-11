package basics

import kotlinx.coroutines.*
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

fun log(msg: String) {
  println("${Thread.currentThread().name}|$msg")
}



object BasicExample {
  @JvmStatic
  fun main(args: Array<String>) {
    val jobA = GlobalScope.launch {
      log("A|waiting")
      delay(100)
      log("A|done")
    }

    val jobB = GlobalScope.launch {
      log("B|done")
    }

    // The default dispatcher has daemon threads,
    // so the app will exit immediately unless we explicitly wait
    runBlocking {
      joinAll(jobA, jobB)
    }
  }
}





object CoroutinesAreLightweightThreads {
  @JvmStatic
  fun main(args: Array<String>) {
    val n = 10_000
    coroutines(n)
    threads(n)
  }

  fun coroutines(n: Int) = runBlocking {
    println("Starting coroutines test")

    val jobs = ArrayList<Job>(n)
    val threadNames = HashSet<String>()
    repeat(n) {
      jobs.add(GlobalScope.launch {
        threadNames.add(Thread.currentThread().name)
        delay(100)
      })
    }

    val elapsed = measureTimeMillis {
      jobs.forEach { it.join() }
    }
    println("Took $elapsed ms to run $n coroutines (seen ${threadNames.count()} thread $threadNames")
  }

  fun threads(n: Int) {
    println("Starting threads test")

    val threads = ArrayList<Thread>(n)
    repeat(n) {
      threads.add(thread {
        Thread.sleep(100)
      })
    }

    val elapsed = measureTimeMillis {
      threads.forEach { it.join() }
    }
    println("Took $elapsed ms to run $n threads")
  }
}


object SuspendFunctions {
  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      println(thisIsASuspendFunction())
    }
  }

  suspend fun thisIsASuspendFunction(): Int {
    delay(100)
    return 10
  }
}