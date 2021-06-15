package ml.ffactory.livecollab.server

import io.ktor.application.*
import io.ktor.http.*
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
      call.respondFile(File("static/index.html"))
    }
    route("/collabs/{collabId}") {
      get {
        val collabId = call.parameters["collabId"] ?: throw IllegalArgumentException("Missing collabId")
        if (CollabManager.get(collabId) != null) {
          call.respondFile(File("static/collab.html"))
        } else {
          call.respondFile(File("static/collab_404.html"))
        }
      }
      post {
        val collabId = call.parameters["collabId"] ?: throw IllegalArgumentException("Missing collabId")
        if (CollabManager.get(collabId) != null) {
          call.respond(HttpStatusCode.Conflict, "Collab already exists")
        } else {
          CollabManager.create(collabId)
          call.respond("OK")
        }
      }
    }

    webSocket("/connect/{collabId}") {
      val collabId = call.parameters["collabId"] ?: return@webSocket
      CollabManager.get(collabId)?.connect(this) ?: return@webSocket
    }
  }
}
