package pl.bartek.scripts.jira.rest.latest.model

import kotlinx.serialization.Serializable

@Serializable
data class Visibility(
    val type: String,
    val value: String,
    val identifier: String
)
