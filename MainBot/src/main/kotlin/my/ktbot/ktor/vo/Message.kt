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
        override val msgType: String get() = sysType
    }

    @Serializable
    @SerialName("msgs")
    class Msgs(val msgs: List<Message> = ArrayList(0)) : Message()

    @Serializable
    @SerialName("roles")
    class Roles(val roles: Map<Int, RoleConfig>) : Message()

}
