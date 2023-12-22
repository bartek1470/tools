package pl.bartek.scripts.jira.rest.latest.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(
    val jql: String,
    val startAt: Int = 0,
    val maxResults: Int = 50,
    val fields: List<String> = listOf(),
    val expand: List<String> = listOf(),
    val validateQuery: Boolean = false,
)
