package pl.bartek.scripts.jira.rest.latest.model

import kotlinx.serialization.Serializable

@Serializable
data class Content(
    val type: String,
    val content: List<Paragraph>
)
