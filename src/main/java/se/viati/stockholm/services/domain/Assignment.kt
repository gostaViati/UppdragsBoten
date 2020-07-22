package se.viati.stockholm.services.domain

data class Assignment(
    val originatingMailIds: List<String>,
    val title: String,
    val description: String,
    val source: String? = null
) {
  constructor(originatingMailId: String, title: String, description: String, source: String? = null) :
      this(listOf(originatingMailId), title, description, source)

  fun addOriginatingIds(newOriginatingMailIds: List<String>) =
      copy(originatingMailIds = originatingMailIds + newOriginatingMailIds)
}
