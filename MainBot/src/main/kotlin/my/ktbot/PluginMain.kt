package my.ktbot

import kotlinx.coroutines.runBlocking
import my.ktbot.annotation.*
import my.ktbot.command.AdminCommand
import my.ktbot.command.CocCommand
import my.ktbot.command.CocSbiCommand
import my.ktbot.command.HelperCommand
import my.ktbot.ktor.RegimentKtorServer
import my.ktbot.plugs.*
import my.ktbot.utils.Counter
import my.ktbot.utils.global.Debugger
import my.miraiplus.MyKotlinPlugin
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.console.command.BuiltInCommands
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 插件入口
 */
object PluginMain : MyKotlinPlugin(
        JvmPluginDescription(
                id = "my.ktbot.binbot",
                version = "0.1"
        ) {
            author("bin.qq=2938137849")
            info("这是一个测试插件,在这里描述插件的功能和用法等.")
        },
        EmptyCoroutineContext
), JvmPlugin {

    override fun PluginComponentStorage.onLoad() {
        Debugger.debug = false
        // EmptySerializersModule()
        PlugConfig.reload()
        logger.info("管理员QQ：${PlugConfig.adminId}")
        logger.info("管理员QQ群：${PlugConfig.adminGroup}")
        injectMap + SendAuto + NeedAdmin + RegexAnn.Inject + LimitAll +
                SendGroup + SendAdmin + NeedExp + NeedAt + HasPerm
        Counter.start(this@PluginMain)
        // ShareCertificateUtil.start(this@PluginMain)
        // Thread {
        // }.start()
        RegimentKtorServer.server(8088).start(false)
        Runtime.getRuntime().addShutdownHook(Thread {
            RegimentKtorServer.regimentServer?.stop()
        })
    }

    override fun onEnable() {
        logger.warning("Plugin loaded")
        logger.info(Counter.members[2938137849].toString())

        arrayOf(
                BotProxy,
                CQBotRepeat,
                MemberExp,
                CQBotBan,
                // CQBotPicture, CQBotPixiv,
                CQBotPerm,
                CQBotHelper,
                CQBotMemeAI,
                // CQBotWhoAtMe,
                CQNginxLogHandle,
                // BotShareCertificate,
        ).forEach {
            register(it)
        }
        register(BotEventHandle)
        Debugger.yes {
            register(DebugPlug)
            logger.debug(callers.mapIndexed { i, c ->
                "\n$i :${c.name}"
            }.joinToString(""))
            runBlocking {
                // MihoyoBbs()
                // HonKai2()
            }
        }
        fun registerCommand(vararg commandss: Array<out Command>) {
            for (commands in commandss) {
                for (command in commands) {
                    CommandManager.registerCommand(command)
                }
            }
        }
        registerCommand(
                AdminCommand.all,
                CocCommand.all,
                CocSbiCommand.all,
                HelperCommand.all,
        )

        // KtorTest.run {
        // 	with(KtorTest) {
        // 		routingArticles()
        // 		routingDate()
        // 	}
        // }.start()
    }

    override fun onDisable() {
        unregisterAll()
        CommandManager.unregisterAllCommands(this)
        Counter.save()
    }

    inline fun <T> catch(block: () -> T): T? {
        return try {
            block()
        } catch (e: Exception) {
            logger.error(e); null
        }
    }

    suspend fun showdown() {
        logger.warning("开始退出")
        with(BuiltInCommands.StopCommand) {
            ConsoleCommandSender.handle()
        }
    }
}
