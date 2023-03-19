package my.ktbot.ktor.dao

import kotlinx.serialization.Serializable

@Serializable
class Tag(val name: String, val type: String = "", val color: String = "")
