package app.editors.manager.googledrive.mvp.models.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateItemRequest(
    val name: String = "",
    val mimeType: String = "",
    val parents: List<String> = emptyList(),
)
