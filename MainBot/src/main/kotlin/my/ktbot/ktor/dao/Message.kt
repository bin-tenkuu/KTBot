package my.ktbot.ktor.dao

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *  @Date:2023/3/11
 *  @author bin
 *  @version 1.0.0
 */
@Serializable
sealed class Message {
    var id: Long? = null

    @Serializable
    @SerialName("text")
    class Text(val msg: String, val role: String) : Message()

    @Serializable
    @SerialName("pic")
    class Pic(val msg: String, val role: String) : Message()

    @Serializable
    @SerialName("roles")
    class Roles(val roles: MutableMap<String?, MutableList<Tag>>) : Message()

    @Serializable
    @SerialName("role")
    class Role(val name: String) : Message()
}
