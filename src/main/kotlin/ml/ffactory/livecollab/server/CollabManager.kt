package ml.ffactory.livecollab.server

import java.util.concurrent.ConcurrentHashMap

object CollabManager {
  private val collabs: MutableMap<String, Collab> = ConcurrentHashMap(LinkedHashMap())

  fun get(collabId: String) = collabs[collabId]

  fun create(collabId: String) = Collab(collabId).also { collabs[collabId] = it }

  /** Returns true if successful */
  fun delete(collabId: String): Boolean {
    return get(collabId)?.let { collab ->
      collabs.remove(collabId)?.also { collab.close() }
    } != null
  }

  fun rename(oldId: String, newId: String): Boolean {
    return get(oldId)?.let { collab ->
      collab.id = newId
      collabs.remove(oldId)
      collabs[newId] = collab
    } != null
  }
}