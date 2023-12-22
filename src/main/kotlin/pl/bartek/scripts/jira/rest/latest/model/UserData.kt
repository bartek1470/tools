package pl.bartek.scripts.jira.rest.latest.model

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val self: String,
    val key: String? = null,
    val accountId: String,
    val accountType: String,
    val name: String? = null,
    val emailAddress: String,
    val avatarUrls: AvatarUrls,
    val displayName: String,
    val active: Boolean,
    val timeZone: String,
    val groups: Groups,
    val applicationRoles: ApplicationRoles
)
