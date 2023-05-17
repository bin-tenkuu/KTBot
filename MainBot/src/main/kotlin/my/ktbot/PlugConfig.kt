package my.ktbot

import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.GroupEvent
import net.mamoe.mirai.event.events.MessageEvent

object PlugConfig : AutoSavePluginConfig("config") {
    val adminId: Long by value(2938137849L)
    val adminGroup: Long by value(391287836L)
    val proxy: Proxy by value(Proxy())
    val nginxLogPath: String by value("./logs")
    val juheApi: Map<String, String> by value(HashMap())
    val openAiToken: String by value("")
    val dataSource: DataSource by value(DataSource())

    fun getAdmin(bot: Bot): Friend {
        return bot.getFriendOrFail(adminId)
    }

    fun getAdminGroup(bot: Bot): Group {
        return bot.getGroupOrFail(adminGroup)
    }

    fun isAdmin(e: MessageEvent): Boolean {
        return e is GroupEvent && e.group.id == adminGroup || e.sender.id == adminId
    }

    @Serializable
    class Proxy {
        var http: String = ""
        var socks: String = ""
        var port: Int = 0
    }

    @Serializable
    class DataSource {
        var url = ""
        var username = ""
        var password = ""
    }

}
