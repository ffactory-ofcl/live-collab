package ml.ffactory.livecollab.server

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "value")
sealed class ServerMessage {
  class Hello(val helloInfo: HelloInfo) : ServerMessage() {
    class HelloInfo(
      val collabName: String,
      val protocolVersion: Int = 1,
    )
  }

  object Okay : ServerMessage()

  object Error : ServerMessage()

  object Closing : ServerMessage()

  class ClientMessagesForwarded(val messages: List<ClientMessage>, val contentHash: Int) : ServerMessage()

  class ContentOverride(val content: String) : ServerMessage()

  class UpdateCollabId(val name: String) : ServerMessage()
}



