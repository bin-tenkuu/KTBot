package my.ktbot.ktor.vo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import my.ktbot.ktor.dao.RoleConfig

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
    @SerialName("default")
    class Default : Message()

    interface Msg {
        val msg: String
    }

    @Serializable
    @SerialName("text")
    class Text(override val msg: String) : Message(), Msg {
        constructor(id: Long, msg: String, role: String) : this(msg) {
            this.id = id
            this.role = role
        }
    }

    @Serializable
    @SerialName("pic")
    class Pic(override val msg: String) : Message(), Msg {
        constructor(id: Long, msg: String, role: String) : this(msg) {
            this.id = id
            this.role = role
        }
    }

    @Serializable
    @SerialName("sys")
    class Sys(override val msg: String) : Message(), Msg {
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

}

@Serializable
class RoomMessage(
        val id: String,
        val name: String,
        val roles: MutableMap<String, RoleConfig>,
)
