package pl.bartek.scripts.jira.rest.latest.model

import kotlinx.serialization.Serializable

@Serializable
data class Author(
    val self: String,
    val accountId: String,
    val displayName: String,
    val active: Boolean
)
