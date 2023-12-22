package pl.bartek.scripts.jira.rest.latest.model

import kotlinx.serialization.Serializable

@Serializable
data class Paragraph(
    val type: String,
    val content: List<Text>
)
