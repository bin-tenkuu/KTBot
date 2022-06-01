package my.ktbot

import kotlinx.coroutines.CompletableJob
import my.ktbot.annotation.*
import my.ktbot.interfaces.Plug
import my.ktbot.plugs.*
import my.ktbot.utils.CacheMap
import my.ktbot.utils.Counter
import my.miraiplus.MyEventHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import java.time.Duration

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
	private val inviteCount = CacheMap<Long, Unit>(Duration.ofHours(12).toMillis())

	@JvmField
	val eventChannel: EventChannel<Event> = GlobalEventChannel.parentScope(this).exceptionHandler(logger::error)

	val myEventHandle = MyEventHandle(this)

	@JvmStatic
	private val eventListeners: ArrayList<CompletableJob> = ArrayList()

	override fun PluginComponentStorage.onLoad() {
		PlugConfig.reload()
		logger.warning("管理员QQ：${PlugConfig.adminId}")
		logger.warning("管理员QQ群：${PlugConfig.adminGroup}")
		Plug += listOf(
			CQBotCOC,
			// CQBotPixiv, CQBotPicture,
			CQBotPerm,
		)
		myEventHandle.injector + SendAuto.Inject + NeedAdmin.Inject + RegexAnn.Inject() +
			SendGroup.Inject + SendAdmin.Inject
	}

	override fun onEnable() {
		logger.warning("Plugin loaded")
		logger.warning(Counter.members[2938137849].toString())
		fun Long.toNow() = Duration.ofMillis(System.currentTimeMillis() - this)

		subscribeAlways<GroupMessageEvent> {
			val millis = System.currentTimeMillis()
			val plug = Plug(this) ?: return@subscribeAlways
			Counter.log(it)
			logger.info("${millis.toNow()}:${plug.name}\t来源:${sender.group.id}.${sender.id}")
			intercept()
		}
		subscribeAlways<FriendMessageEvent> {
			val millis = System.currentTimeMillis()
			val plug = Plug(this) ?: return@subscribeAlways
			Counter.log(it)
			logger.info("${millis.toNow()}:${plug.name}\t来源:${sender.id}")
			intercept()
		}
		myEventHandle += arrayOf(
			CQBotSBI, BotProxy,//	CQBotCOC, CQBotSBI,
			CQBotRepeat, AddExp, MemberExp, CQBotBan,
			//	CQBotPixiv, CQBotPicture,
			CQBotHelper, CQBotListGet, CQBotMemeAI,//	CQBotPerm, CQBotHelper,
			CQNginxLogHandle
		)
		myEventHandle += arrayOf(
			BotEventHandle
		)
		println(myEventHandle.callers.joinToString(",") { it.name })
	}

	override fun onDisable() {
		myEventHandle.unregisterAll()
		eventListeners.removeIf { it.complete();true }
		Counter.save()
	}

	@JvmStatic
	private inline fun <reified E : Event> subscribeAlways(
		concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
		priority: EventPriority = EventPriority.NORMAL,
		noinline handler: suspend E.(E) -> Unit,
	): Listener<E> {
		return eventChannel.subscribeAlways(E::class, coroutineContext, concurrency, priority, handler).also {
			eventListeners += it
		}
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
