package ml.ffactory.livecollab.server

import java.util.*

object CollabManager {
  private val collabs: MutableMap<String, Collab> = Collections.synchronizedMap(LinkedHashMap())

  operator fun get(collabId: String): Collab {
    return collabs[collabId] ?: Collab(collabId).also { collabs[collabId] = it }
  }
}