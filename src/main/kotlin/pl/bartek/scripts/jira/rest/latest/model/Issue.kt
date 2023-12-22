package pl.bartek.scripts.jira.rest.latest.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Issue(
    val id: String,
    val key: String,
    val fields: Map<String, JsonElement?>,
    val renderedFields: Map<String, String?> = mapOf(),
    val self: String,
    @Serializable(with = ExpandListSerializer::class)
    val expand: List<Expand>
)
