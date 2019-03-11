package basics

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

object ScopeExample {
  class ExampleServerOfSomeKind : CoroutineScope {
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
      get() = Dispatchers.Default + job

    fun start() {
      job = Job()
      println("Server job $job")
    }

    suspend fun stop() {
      job.cancelAndJoin()
    }

    fun doA() = launch {
      println("A done")
    }

    fun doB() = launch {
      println("B waiting")
      delay(1000)
      println("B done")
    }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val s = ExampleServerOfSomeKind()
    s.start()

    s.doA()
    s.doB()

    runBlocking {
      s.stop()
    }
  }
}
