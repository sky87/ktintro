import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerCollection
import util.handler
import util.parseJsonBody
import util.sendJSON
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.servlet.AsyncEvent
import javax.servlet.AsyncListener
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// Need to have request classes out here for Jackson
class SendReq(val user: String, val chat: String, val data: String)
class GetUpdatesReq(val chat: String, val receivedCount: Int)

object ChatPolling {
  @JvmStatic
  fun main(args: Array<String>) {
    val handlers = HandlerCollection()

    data class Message(val user: String, val data: String) { val ts = ZonedDateTime.now() }
    class Chat { val messages = ArrayList<Message>() }
    val chatByName = HashMap<String, Chat>()

    handlers.addHandler(handler("POST", "/send") { req, res ->
      val sendReq: SendReq = parseJsonBody(req)
      val chat = synchronized(chatByName) {
        chatByName.getOrPut(sendReq.chat) {Chat()}
      }
      synchronized(chat) {
        val msg = Message(sendReq.user, sendReq.data)
        chat.messages.add(msg)
        println("Added $msg to ${sendReq.chat}")
      }
    })

    handlers.addHandler(handler("GET", "/updates") { req, res ->
      val getUpdatesReq: GetUpdatesReq = parseJsonBody(req)
      val chat = synchronized(chatByName) {
        chatByName[getUpdatesReq.chat]
      }
      if (chat != null) {
        val newMessages = synchronized(chat) {
          chat.messages.drop(getUpdatesReq.receivedCount)
        }
        sendJSON(res, newMessages)
      }
      else {
        sendJSON(res, emptyList<Unit>())
      }
    })

    val jetty = Server(9090)
    jetty.handler = handlers
    jetty.start()
    println("Server (polling) listening")
    jetty.join()
  }

}

object ChatLongPolling {
  @JvmStatic
  fun main(args: Array<String>) {
    val handlers = HandlerCollection()

    data class Message(val user: String, val data: String) { val ts = ZonedDateTime.now() }
    class Chat {
      val lock = ReentrantLock()
      val hasData = lock.newCondition()
      val messages = ArrayList<Message>()
    }
    val chatByName = HashMap<String, Chat>()

    handlers.addHandler(handler("POST", "/send") { req, res ->
      val sendReq: SendReq = parseJsonBody(req)
      val chat = synchronized(chatByName) {
        chatByName.getOrPut(sendReq.chat) {Chat()}
      }
      chat.lock.withLock {
        val msg = Message(sendReq.user, sendReq.data)
        chat.messages.add(msg)
        chat.hasData.signalAll()
        println("Added $msg to ${sendReq.chat}")
      }
    })

    handlers.addHandler(handler("GET", "/updates") { req, res ->
      val getUpdatesReq: GetUpdatesReq = parseJsonBody(req)
      val chat = synchronized(chatByName) {
        chatByName[getUpdatesReq.chat]
      }
      if (chat != null) {
        val newMessages = chat.lock.withLock {
          while (chat.messages.size <= getUpdatesReq.receivedCount) {
            chat.hasData.await(5, TimeUnit.SECONDS)
          }
          chat.messages.drop(getUpdatesReq.receivedCount)
        }
        sendJSON(res, newMessages)
      }
      else {
        sendJSON(res, emptyList<Unit>())
      }
    })

    val jetty = Server(9090)
    jetty.handler = handlers
    jetty.start()
    println("Server (long polling) listening")
    jetty.join()
  }
}

object ChatCoroutines {
  @JvmStatic
  fun main(args: Array<String>) {
    val handlers = HandlerCollection()

    data class Message(val user: String, val data: String) { val ts = ZonedDateTime.now() }
    class Chat {
      val lock = ReentrantLock()
      val continuations = ArrayList<Continuation<Unit>>()
      val messages = ArrayList<Message>()
    }
    val chatByName = HashMap<String, Chat>()

    handlers.addHandler(handler("POST", "/send") { req, res ->
      val sendReq: SendReq = parseJsonBody(req)
      val chat = synchronized(chatByName) {
        chatByName.getOrPut(sendReq.chat) {Chat()}
      }
      chat.lock.withLock {
        val msg = Message(sendReq.user, sendReq.data)
        chat.messages.add(msg)
        chat.continuations.forEach { it.resume(Unit) }
        chat.continuations.clear()
        println("Added $msg to ${sendReq.chat}")
      }
    })

    handlers.addHandler(handler("GET", "/updates") { req, res ->
      val getUpdatesReq: GetUpdatesReq = parseJsonBody(req)
      val chat = synchronized(chatByName) {
        chatByName[getUpdatesReq.chat]
      }
      if (chat != null) {

        val httpAsyncCtx = req.startAsync()
        GlobalScope.launch {
          var newMessages: List<Message> = listOf()

          while (newMessages.isEmpty()) {
            suspendCoroutine<Unit> { cont ->
              chat.lock.withLock {
                if (chat.messages.size > getUpdatesReq.receivedCount) {
                  newMessages = chat.messages.drop(getUpdatesReq.receivedCount)
                  cont.resume(Unit)
                }
                else {
                  chat.continuations.add(cont)
                  // TODO handle socket closes!
                }
              }
            }
          }

          launch(Dispatchers.IO) {
            sendJSON(res, newMessages)
          }
        }.invokeOnCompletion {
          // TODO handle exceptions!
          httpAsyncCtx.complete()
        }
      }
      else {
        sendJSON(res, emptyList<Unit>())
      }
    })

    val jetty = Server(9090)
    jetty.handler = handlers
    jetty.start()
    println("Server (coroutines) listening")
    jetty.join()
  }
}
