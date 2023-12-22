package pl.bartek.scripts.jira.rest.latest.model

import kotlinx.serialization.Serializable

@Serializable
data class WorklogItem(
    val self: String,
    val author: Author,
    val updateAuthor: Author,
    val updated: String,
    val visibility: Visibility? = null,
    val started: String,
    val timeSpent: String,
    val timeSpentSeconds: Int,
    val id: String,
    val issueId: String
)
