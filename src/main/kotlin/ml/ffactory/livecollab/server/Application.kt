package ml.ffactory.livecollab.server

import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import java.io.File


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
  install(WebSockets)
  routing {


    static("static") {

      files("static")

      default("static/404.html")
    }
    get("") {
      this.call.respondFile(File("static/index.html"))
    }
    get("/collabs/{collabId}") {
      this.call.respondFile(File("static/collab.html"))
    }

    webSocket("/connect/{collabId}") {
      val collabId = call.parameters["collabId"] ?: return@webSocket
      CollabManager[collabId].connect(this)

    }
  }
}
