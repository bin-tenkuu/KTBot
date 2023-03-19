package my.ktbot.ktor.vo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import my.ktbot.ktor.dao.RoleConfig
import my.ktbot.ktor.dao.Tag

/**
 *  @Date:2023/3/11
 *  @author bin
 *  @version 1.0.0
 */
@Serializable
sealed class Message {
    var id: Long? = null
    var role: String = ""

    @Serializable
    @SerialName("text")
    class Text(val msg: String) : Message() {
        constructor(id: Long, msg: String, role: String) : this(msg) {
            this.id = id
            this.role = role
        }
    }

    @Serializable
    @SerialName("pic")
    class Pic(val msg: String) : Message() {
        constructor(id: Long, msg: String, role: String) : this(msg) {
            this.id = id
            this.role = role
        }
    }

    @Serializable
    @SerialName("msgs")
    class Msgs(val msgs: List<Message> = ArrayList(0)) : Message()

    @Serializable
    @SerialName("roles")
    class Roles(val roles: MutableMap<String, RoleConfig>) : Message()

    @Serializable
    @SerialName("role")
    class Role : Message()

    @Serializable
    @SerialName("his")
    class History : Message()
}

@Serializable
class RoomMessage(
    val id: String,
    val name: String,
    val roles: MutableMap<String, RoleConfig>,
)
