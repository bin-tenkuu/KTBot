package my.ktbot

import my.ktbot.annotation.*
import my.ktbot.plugs.*
import my.ktbot.utils.Counter
import my.miraiplus.Caller
import my.miraiplus.MyEventHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.Event

/**
 * 插件入口
 */
object PluginMain : KotlinPlugin(
	JvmPluginDescription(
		id = "my.ktbot.binbot",
		version = "0.1"
	) {
		author("bin.qq=2938137849")
		info("这是一个测试插件,在这里描述插件的功能和用法等.")
	}
), JvmPlugin {

	private val myEventHandle = MyEventHandle(this)
	val callers: List<Caller> get() = myEventHandle.callers

	override fun PluginComponentStorage.onLoad() {
		PlugConfig.reload()
		logger.warning("管理员QQ：${PlugConfig.adminId}")
		logger.warning("管理员QQ群：${PlugConfig.adminGroup}")
		myEventHandle.injector + SendAuto + NeedAdmin + RegexAnn.Inject() +
			SendGroup + SendAdmin + NeedExp + NeedAtBot + HasPerm //+ CheckPerm
		println(myEventHandle.injector[Event::class].joinToString(" -> ") { it.javaClass.name })
	}

	override fun onEnable() {
		logger.warning("Plugin loaded")
		logger.warning(Counter.members[2938137849].toString())

		myEventHandle += arrayOf(
			CQBotCOC, CQBotCOCSBI, BotProxy,
			CQBotRepeat, MemberExp, CQBotBan,
			CQBotPicture, CQBotPixiv,
			CQBotPerm, CQBotHelper, CQBotListGet, CQBotMemeAI,
			CQNginxLogHandle
		)
		myEventHandle += arrayOf(
			BotEventHandle
		)
		logger.info(callers.mapIndexed { i, c -> "\t$i :${c.name}" }.joinToString(""))
	}

	override fun onDisable() {
		myEventHandle.unregisterAll()
		Counter.save()
	}

	inline fun <T> catch(block: () -> T): T? {
		return try {
			block()
		}
		catch (e: Exception) {
			logger.error(e); null
		}
	}
}
