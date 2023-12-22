package pl.bartek.scripts.jira.rest.latest.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvatarUrls(
    @SerialName("48x48")
    val x48: String,

    @SerialName("24x24")
    val x24: String,

    @SerialName("16x16")
    val x16: String,

    @SerialName("32x32")
    val x32: String
)
