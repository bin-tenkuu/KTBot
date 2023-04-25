package my.ktbot

import kotlinx.serialization.SerialName
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
    val mihoyo: Mihoyo by value(Mihoyo())
    val dataSource: DataSource by value(DataSource())

    fun getAdmin(bot: Bot): Friend {
        return bot.getFriendOrFail(adminId)
    }

    fun getAdminGroup(bot: Bot): Group {
        return bot.getGroupOrFail(adminGroup)
    }

    fun isAdmin(e: MessageEvent): Boolean =
        if (e is GroupEvent && e.group.id == adminGroup) true else e.sender.id == adminId

    @Serializable
    class Proxy {
        var http: String = ""
        var socks: String = ""
        var port: Int = 0
    }

    @Serializable
    class DataSource(
        var url: String = "",
        var username: String = "",
        var password: String = "",
    )

    @Serializable
    class Mihoyo(
        var enable: Boolean = false,
        var cookie: String = "",
        @SerialName("login_ticket")
        var loginTicket: String = "",
        var stuid: String = "",
        var stoken: String = "",
        val games: MutableMap<String, Boolean> = HashMap(),
    ) {
        fun clear_cookies() {
            enable = false
            loginTicket = ""
            stuid = ""
            stoken = ""
            cookie = "CookieError"
        }

        fun clear_cookie_game(game_id: String) {
            games[game_id] = false
        }
    }
}
