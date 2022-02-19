package my.ktbot.plugin

import kotlinx.coroutines.*
import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.plugs.*
import my.ktbot.plugin.plugs.subPlugs.*
import my.ktbot.plugin.utils.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.PlainText
import java.time.Duration

/**
 * 插件入口
 */
object PluginMain : KotlinPlugin(
	JvmPluginDescription(
		id = "my.ktbot.plugin.binbot",
		version = "0.1"
	) {
		author("bin.qq=2938137849")
		info("这是一个测试插件,在这里描述插件的功能和用法等.")
	}
), JvmPlugin {
	private var inviteCount = CacheMap<Long, Unit>(Duration.ofHours(12).toMillis())

	@JvmStatic
	private val eventChannel: EventChannel<Event> by lazy {
		GlobalEventChannel.parentScope(this).exceptionHandler {
			logger.error(it)
		}
	}

	@JvmStatic
	private val eventListeners: ArrayList<CompletableJob> = ArrayList()

	@JvmStatic
	private var tasker: Job? = null

	override fun PluginComponentStorage.onLoad() {
//		val logger = MiraiLogger.Factory.create(javaClass, "test")
		PlugConfig.reload()
		logger.warning("管理员QQ：${PlugConfig.adminId}")
		logger.warning("管理员QQ群：${PlugConfig.adminGroup}")
		@Suppress("RemoveRedundantSpreadOperator")
		Plug += mutableListOf(
			CQBotCOC, *arrayOf(COCCheater, COCStat, COCStatSet, COCAdded, COCSpecial),
			CQBotRepeat, MemeAI, AddExp, MemberExp, CQBotBan, CQBotPixiv,
			CQBotPicture, *arrayOf(SeTuCache, SeTuSet),
			*CQBotMSG.list, *CQBotPlugin.list,
			CQBotCounter, CQBotHelper, CQBotCalculate
		)
	}

	override fun onEnable() {
		logger.warning("Plugin loaded")
		Counter.members[2938137849].run {
			logger.warning(toString())
		}
		subscribeAlways<GroupMessageEvent> {
			val millis = System.currentTimeMillis()
			val plug = Plug(this) ?: return@subscribeAlways
			Counter.log(this, plug)
			logger.info("${
				Duration.ofMillis(System.currentTimeMillis() - millis)
			}:${plug.name}\t来源:${sender.group.id}.${sender.id}")
		}
		subscribeAlways<FriendMessageEvent> {
			val millis = System.currentTimeMillis()
			val plug = Plug(this) ?: return@subscribeAlways
			Counter.log(this, plug)
			logger.info("${
				Duration.ofMillis(System.currentTimeMillis() - millis)
			}:${plug.name}\t来源:${sender.id}")
		}
		subEvents()
	}

	override fun onDisable() {
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

	private fun subEvents() {
		subscribeAlways<BotOnlineEvent> {
			bot.startCounter()
			logger.info("${bot.nameCardOrNick} 已上线")
		}
		subscribeAlways<BotOfflineEvent> {
			tasker?.cancel()
			tasker = null
			logger.info("${bot.nameCardOrNick} 已下线")
		}
		subscribeAlways<BotReloginEvent> {
			bot.startCounter()
			logger.info("${bot.nameCardOrNick} 已重新登录")
		}
		subscribeAlways<NewFriendRequestEvent> {
			sendAdmin("${fromNick}（${
				fromId
			}）来自群 ${fromGroup?.name ?: ""}（${
				fromGroupId
			}）请求添加好友消息：\n${message}")
			if (inviteCount.size <= 10) {
				inviteCount[fromId] = Unit
				//自动同意好友申请
				accept()
			}
			else reject()
		}
		subscribeAlways<BotInvitedJoinGroupRequestEvent> {
			sendAdmin("${invitorNick}（${invitorId}）邀请加入群 ${groupName}（${groupId}）")
			Counter.groups[groupId].update {
				exp = 20.0
				invited = invitorId
			}
//			if (inviteCount.size <= 10) {
//				inviteCount[groupId] = Unit
//				//自动同意加群申请
//				accept()
//			}
		}
		subscribeAlways<MemberJoinEvent> {
			val msg = "@${member.nick} " + when (this) {
				is MemberJoinEvent.Invite -> "被 @${invitor.nick} 邀请"
				is MemberJoinEvent.Active -> "欢迎"
				else -> ""
			} + "入群"
			try {
				group.sendMessage(PlainText(msg))
			} catch (e: Exception) {
				logger.error(toString(), e)
				sendAdmin("来自群：${groupId}\n${msg}")
			}
		}
		subscribeAlways<MemberLeaveEvent> {
			val msg = "@${member.nick}(${member.id})" + when (this) {
				is MemberLeaveEvent.Quit -> "主动离开本群"
				is MemberLeaveEvent.Kick -> "被 管理员(@${operator?.nick ?: bot.nick}) 踢出本群"
				else -> "未知方式离开本群"
			}
			try {
				group.sendMessage(PlainText(msg))
			} catch (e: Exception) {
				logger.error(toString(), e)
				sendAdmin("来自群：${groupId}\n${msg}")
			}
		}
		subscribeAlways<BotLeaveEvent> {
			sendAdmin("bot被踢出群：${groupId}(${group.name})")
		}
		subscribeAlways<OtherClientOnlineEvent> {
			sendAdmin("""其他客户端上线
				|设备名称:${client.info.deviceName}
				|设备类型:${client.info.deviceKind}
			""".trimMargin())
		}
		subscribeAlways<OtherClientOnlineEvent> {
			sendAdmin("""其他客户端下线
				|设备名称:${client.info.deviceName}
				|设备类型:${client.info.deviceKind}
			""".trimMargin())
		}
		subscribeAlways<BotMuteEvent> {
			Counter.groups[groupId].update { isBaned = true }
		}
		subscribeAlways<BotUnmuteEvent> {
			Counter.groups[groupId].update { isBaned = false }
		}
	}

	@JvmStatic
	private fun Bot.startCounter() {
		tasker?.cancel()
		tasker = launch(coroutineContext, CoroutineStart.UNDISPATCHED) {
			logger.warning("携程启动")
			delay(Duration.ofHours(1).toMillis())
			while (bot.isOnline) {
				val group = PlugConfig.getAdminGroup(bot)
				group.sendMessage(Counter.state(group))
				Counter.clear()
				delay(Duration.ofHours(2).toMillis())
			}
			logger.warning("bot下线")
			tasker = null
		}
	}
}
