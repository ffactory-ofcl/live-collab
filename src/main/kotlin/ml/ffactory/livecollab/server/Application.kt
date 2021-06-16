package ml.ffactory.livecollab.server

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
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
      patch {
        val updateRequest = call.receiveOrNull<UpdateCollabNameRequest>() ?: throw IllegalArgumentException("Missing patch body")
        val collabId = call.parameters["collabId"] ?: throw IllegalArgumentException("Missing collabId")
        val collab = CollabManager.get(collabId)
        if (collab != null) {
          collab.collabId = updateRequest.name
        } else {
          call.respond(HttpStatusCode.NotFound, "Collab does not exist")
        }
      }
      delete {
        val collabId = call.parameters["collabId"] ?: throw IllegalArgumentException("Missing collabId")
        if (CollabManager.get(collabId) != null) {
          CollabManager.delete(collabId)
        } else {
          call.respond(HttpStatusCode.NotFound, "Collab does not exist")
        }
      }
    }

    webSocket("/connect/{collabId}") {
      val collabId = call.parameters["collabId"] ?: return@webSocket
      CollabManager.get(collabId)?.connect(this) ?: return@webSocket
    }
  }
}

class UpdateCollabNameRequest(val name: String)
