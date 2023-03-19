package my.ktbot.ktor.dao

import kotlinx.serialization.Serializable

@Serializable
class RoleConfig(
    val id: String,
    var name: String,
    var tags: MutableList<Tag>,
)
