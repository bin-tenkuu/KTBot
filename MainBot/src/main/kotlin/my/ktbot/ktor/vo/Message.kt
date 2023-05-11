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

    companion object {
        const val textType = "text"
        const val picType = "pic"
        const val sysType = "sys"
    }

    sealed interface Msg {
        val msgType: String
        var id: Long?
        var role: Int
        val msg: String
    }

    @Serializable
    @SerialName("default")
    open class Default(
            var id: Long? = null,
            var role: Int
    ) : Message()

    @Serializable
    @SerialName(textType)
    class Text(
            override val msg: String,
            override var id: Long? = null,
            override var role: Int = -1
    ) : Msg, Message() {
        override val msgType: String get() = textType
    }

    @Serializable
    @SerialName(picType)
    class Pic(
            override val msg: String,
            override var id: Long? = null,
            override var role: Int = -1
    ) : Msg, Message() {
        override val msgType: String get() = picType
    }

    @Serializable
    @SerialName(sysType)
    class Sys(
            override val msg: String,
            override var id: Long? = null,
            override var role: Int = -1
    ) : Msg, Message() {
        constructor(id: Long, msg: String, role: Int) : this(msg) {
            this.id = id
            this.role = role
        }

        override val msgType: String get() = sysType
    }

    @Serializable
    @SerialName("msgs")
    @Suppress("unused")
    class Msgs(val msgs: List<Message> = ArrayList(0)) : Message() {
    }

    @Serializable
    @SerialName("roles")
    @Suppress("unused")
    class Roles(val roles: Map<Int, RoleConfig>) : Message() {
    }

    @Serializable
    @SerialName("role")
    @Suppress("unused")
    class Role(val role: Int, val name: String, val color: String = "") : Message() {
        constructor(role: RoleConfig) : this(role.id, role.name, role.color)
    }

}
