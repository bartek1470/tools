package pl.bartek.scripts.jira.rest.latest.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val total: Int,
    val startAt: Int,
    val maxResults: Int,
    val issues: List<Issue>,
    @Serializable(with = ExpandListSerializer::class)
    val expand: List<Expand>,
)
