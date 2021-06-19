package ml.ffactory.livecollab.server

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking

class Connection(
  private val session: DefaultWebSocketSession,
  private val collab: Collab,
) {
  private val json = ObjectMapper()
      .registerModule(KotlinModule())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  init {
    send(ServerMessage.Hello(ServerMessage.Hello.HelloInfo(collab.id)))
    send(ServerMessage.ContentOverride(collab.content))
  }

  fun forward(messages: List<ClientMessage>) = send(
    ServerMessage.ClientMessagesForwarded(messages, collab.content.hashCode())
  )

  fun send(msg: ServerMessage) {
    val msgStr = json.writeValueAsString(msg)
    runBlocking {
      session.send(msgStr)
    }
  }

  suspend fun start() {
    try {
      for (frame in session.incoming) {
        frame as? Frame.Text ?: continue
        val msgStr = frame.readText()
        receivedText(this, msgStr)
      }
    } catch (e: Exception) {
      System.err.println(e.localizedMessage)
    } finally {
      collab.disconnect(this)
    }
  }

  private suspend fun receivedText(connection: Connection, msgStr: String) {
    try {
      val messages = msgStr.deserialize<List<ClientMessage>>()

      collab.receivedMessages(connection, messages)
    } catch (e: Exception) {
      e.printStackTrace()
      send(ServerMessage.Error)
    }
  }

  private inline fun <reified T> String.deserialize(): T = json.readValue(this)

}