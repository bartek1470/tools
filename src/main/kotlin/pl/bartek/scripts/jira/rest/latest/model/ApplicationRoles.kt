package pl.bartek.scripts.jira.rest.latest.model

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationRoles(
    val size: Int,
    val items: List<String>
)
