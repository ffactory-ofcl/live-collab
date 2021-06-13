package ml.ffactory.livecollab.server

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "value")
sealed class ClientMessage {
  data class TextEdit(
    val location: Int,
    val action: TextEditAction,
  ) : ClientMessage()

  object Refresh : ClientMessage()
}

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "value")
sealed class TextEditAction {
  data class Insert(val s: String) : TextEditAction()

  data class Delete(val c: Int) : TextEditAction()

  data class Replace(val s: String) : TextEditAction()
}




