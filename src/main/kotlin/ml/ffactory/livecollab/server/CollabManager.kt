package ml.ffactory.livecollab.server

import java.util.*

object CollabManager {
  private val collabs: MutableMap<String, Collab> = Collections.synchronizedMap(LinkedHashMap())

  fun get(collabId: String) = collabs[collabId]

  fun create(collabId: String) = Collab(collabId).also { collabs[collabId] = it }
}