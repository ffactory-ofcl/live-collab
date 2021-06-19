package ml.ffactory.livecollab.server

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class Collab(
  id: String,
  var content: String = Constants.collabStartupContent,
  private val connections: MutableSet<Connection> = Collections.synchronizedSet(LinkedHashSet()),
) {
  var id: String = id
    set(value) {
      field = value
      updatedId()
    }

  suspend fun connect(session: DefaultWebSocketSession) {
    val connection = Connection(session, this)
    connections += connection
    connection.start()
  }

  fun disconnect(connection: Connection) {
    connections -= connection
  }

  private fun updatedId() {
    connections.forEach { it.send(ServerMessage.UpdateCollabId(this.id)) }
  }

  @Synchronized
  suspend fun receivedMessages(connection: Connection, messages: List<ClientMessage>) {
    val messagesToForward = messages.mapNotNull {
      receivedMessage(connection, it)
    }
    connections.forEach { otherConnection ->
      if (otherConnection != connection) {
        otherConnection.forward(messagesToForward)
      }
    }
  }

  @Synchronized
  private suspend fun receivedMessage(connection: Connection, msg: ClientMessage): ClientMessage? {
    fun Int.coerce(): Int = this.coerceIn(0, content.length) // coerceAtLeast(1) - 1

    println("Collab $id received $msg")
    when (msg) {
      is ClientMessage.TextEdit -> {
        content = when (msg.action) {
          is TextEditAction.Insert -> {
            if (msg.location.coerce() != msg.location) resendContentLater(connection)

            content.substring(0, msg.location.coerce()) + msg.action.s + content.substring(msg.location.coerce())
          }
          is TextEditAction.Delete -> {
            if (msg.location.coerce() != msg.location) resendContentLater(connection)

            content.substring(0, (msg.location - msg.action.c).coerce()) + content.substring(msg.location.coerce())
          }
          is TextEditAction.Replace -> msg.action.s
        }
        println("content:\n")
        return msg
      }
      ClientMessage.Refresh -> {
        connection.send(ServerMessage.ContentOverride(content))
      }
    }
    return null
  }

  private suspend fun resendContentLater(connection: Connection) {
    coroutineScope {
      launch {
        delay(200)
        connection.send(ServerMessage.ContentOverride(content))
      }
    }
  }

  fun close() {
    connections.forEach {
      it.send(ServerMessage.Closing)
      disconnect(it)
    }
  }

}