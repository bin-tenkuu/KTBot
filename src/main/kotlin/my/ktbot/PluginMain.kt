package my.ktbot

import kotlinx.coroutines.*
import my.ktbot.interfaces.Plug
import my.ktbot.plugs.*
import my.ktbot.utils.*
import my.miraiplus.MyEventHandle
import my.miraiplus.annotation.MessageHandle
import my.miraiplus.injector.Injector
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.utils.MiraiExperimentalApi
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
	private var inviteCount = CacheMap<Long, Unit>(Duration.ofHours(12).toMillis())

	@JvmField
	val eventChannel: EventChannel<Event> = GlobalEventChannel.parentScope(this).exceptionHandler(logger::error)

	private val myEventHandle = MyEventHandle(this)

	@JvmStatic
	private val eventListeners: ArrayList<CompletableJob> = ArrayList()

	@JvmStatic
	private var tasker: Job? = null

	override fun PluginComponentStorage.onLoad() {
		PlugConfig.reload()
		logger.warning("管理员QQ：${PlugConfig.adminId}")
		logger.warning("管理员QQ群：${PlugConfig.adminGroup}")
		Plug += listOf(
			CQBotCOC, CQBotSBI,
			CQBotRepeat, AddExp, MemberExp, CQBotBan,
			CQBotPixiv, CQBotPicture,
			CQBotPerm, CQBotHelper, CQBotListGet, CQBotMemeAI,
			CQNginxLogHandle,
		)
	}

	override fun onEnable() {
		logger.warning("Plugin loaded")
		logger.warning(Counter.members[2938137849].toString())
		fun Long.toNow() = Duration.ofMillis(System.currentTimeMillis() - this)

		subscribeAlways<GroupMessageEvent> {
			val millis = System.currentTimeMillis()
			val plug = Plug(this) ?: return@subscribeAlways
			logger.info("${millis.toNow()}:${plug.name}\t来源:${sender.group.id}.${sender.id}")
		}
		subscribeAlways<FriendMessageEvent> {
			val millis = System.currentTimeMillis()
			val plug = Plug(this) ?: return@subscribeAlways
			logger.info("${millis.toNow()}:${plug.name}\t来源:${sender.id}")
		}
		subscribeAlways<MessageEvent> { Counter.log(it) }
		subEvents()
		myEventHandle.register(this, ::test)
		myEventHandle.injector.add(object : Injector<MessageHandle>() {
			override fun doAfter(ann: MessageHandle, event: MessageEvent, result: Any?) {
				async { result.toMassage()?.let { event.subject.sendMessage(it) } }
			}
		})
	}

	@MessageHandle("^[.．。]1")
	private fun test(): Int {
		return 1
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
			startCounter()
			// 开启所有列表缓存
			bot.configuration.enableContactCache()
			logger.info("${bot.nameCardOrNick} 已上线")
		}
		subscribeAlways<BotOfflineEvent> {
			val msg = "${bot.nameCardOrNick} 已下线, 是否重连: ${reconnect}, 原因: ${
				when (this) {
					is BotOfflineEvent.Active -> "主动离线, 错误: ${cause}"
					is BotOfflineEvent.Force -> "被挤下线, title: ${title}, message: ${message}"
					// is BotOfflineEvent.MsfOffline -> "主动离线, 错误: ${cause}"
					is BotOfflineEvent.Dropped -> "因网络问题而掉线, 错误: ${cause}"
					// is BotOfflineEvent.RequireReconnect -> "主动离线, 错误: ${cause}"
					else -> toString()
				}
			}"
			startCounter()
			logger.info(msg)
		}
		subscribeAlways<BotReloginEvent> {
			logger.info("${bot.nameCardOrNick} 已重新登录")
		}
		subscribeAlways<NewFriendRequestEvent> {
			val msg = "${fromNick}（${fromId}）来自群 ${fromGroup?.name ?: ""}（${fromGroupId}）请求添加好友消息：\n${message}"
			logger.info("NewFriendRequestEvent: ${msg}")
			sendAdmin(msg)
			if (inviteCount.size <= 10) {
				inviteCount[fromId] = Unit
				//自动同意好友申请
				accept()
			}
			else reject()
		}
		subscribeAlways<BotInvitedJoinGroupRequestEvent> {
			val msg = "${invitorNick}（${invitorId}）邀请加入群 ${groupName}（${groupId}）"
			logger.info("BotInvitedJoinGroupRequestEvent: ${msg}")
			sendAdmin(msg)
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
			val msg = "@${member.nick} ${
				when (this) {
					is MemberJoinEvent.Invite -> "被 @${invitor.nick} 邀请"
					is MemberJoinEvent.Active -> "欢迎"
					is MemberJoinEvent.Retrieve -> "恢复群主身份"
					else -> ""
				}
			}入群"
			logger.info("MemberJoinEvent: ${msg}")
			try {
				group.sendMessage(PlainText(msg))
			}
			catch (e: Exception) {
				logger.error(toString(), e)
				sendAdmin("来自群：${groupId}\n${msg}")
			}
		}
		@OptIn(MiraiExperimentalApi::class)
		subscribeAlways<BotJoinGroupEvent> {
			val msg = "bot成功加入群： ${group.name}(${groupId}), 来源：${
				when (this) {
					is BotJoinGroupEvent.Invite -> "邀请人： ${invitor.nick}(${invitor.id})"
					is BotJoinGroupEvent.Active -> "不确定"
					is BotJoinGroupEvent.Retrieve -> "恢复群主身份"
					else -> ""
				}
			}"
			logger.info("BotJoinGroupEvent: ${msg}")
			sendAdmin(msg)
		}
		subscribeAlways<MemberLeaveEvent> {
			val msg = "@${member.nick}(${member.id})${
				when (this) {
					is MemberLeaveEvent.Quit -> "主动离开本群"
					is MemberLeaveEvent.Kick -> "被 管理员(@${operator?.nick ?: bot.nick}) 踢出本群"
					else -> "未知方式离开本群"
				}
			}"
			logger.info("MemberLeaveEvent: ${msg}")
			try {
				group.sendMessage(PlainText(msg))
			}
			catch (e: Exception) {
				logger.error(toString(), e)
				sendAdmin("来自群：${groupId}\n${msg}")
			}
		}
		@OptIn(MiraiExperimentalApi::class)
		subscribeAlways<BotLeaveEvent> {
			val msg = "bot被踢出群：${groupId}(${group.name}), 原因：${
				when (this) {
					is BotLeaveEvent.Active -> "主动退出, 有被踢出可能"
					is BotLeaveEvent.Kick -> "被踢出群"
					is BotLeaveEvent.Disband -> "群主解散群聊"
					else -> ""
				}
			}"
			logger.info("BotLeaveEvent: ${msg}")
			sendAdmin(msg)
		}
		subscribeAlways<OtherClientOnlineEvent> {
			val msg = """其他客户端上线
				|设备名称:${client.info.deviceName}
				|设备类型:${client.info.deviceKind}
			""".trimMargin()
			logger.info("OtherClientOnlineEvent: ${msg}")
			sendAdmin(msg)
		}
		subscribeAlways<OtherClientOnlineEvent> {
			val msg = """其他客户端下线
				|设备名称:${client.info.deviceName}
				|设备类型:${client.info.deviceKind}
			""".trimMargin()
			logger.info("OtherClientOnlineEvent: ${msg}")
			sendAdmin(msg)
		}
		subscribeAlways<BotMuteEvent> {
			val msg = "bot被禁言，群: ${group.name}(${groupId}), 操作人: ${operator.nameCardOrNick}(${operator.id})"
			logger.info("BotMuteEvent: ${msg}")
			Counter.groups[groupId].update { isBaned = true }
		}
		subscribeAlways<BotUnmuteEvent> {
			val msg = "bot被取消禁言，群: ${group.name}(${groupId}), 操作人: ${operator.nameCardOrNick}(${operator.id})"
			logger.info("BotUnmuteEvent: ${msg}")
			Counter.groups[groupId].update { isBaned = false }
		}
	}

	@JvmStatic
	private fun startCounter() {
		tasker?.cancel()
		if (Bot.instances.isEmpty()) {
			logger.error("无可用bot")
			tasker = null
			return
		}
		tasker = launch(coroutineContext, CoroutineStart.UNDISPATCHED) {
			logger.warning("携程启动")
			while (true) {
				val bot = Bot.instances.firstOrNull() ?: break
				delay(Duration.ofHours(1).toMillis())
				while (bot.isOnline) {
					val group = PlugConfig.getAdminGroup(bot)
					Counter.state(group).also {
						if (!it.isContentEmpty()) group.sendMessage(it)
					}
					Counter.clear()
					delay(Duration.ofHours(2).toMillis())
				}
				delay(Duration.ofHours(1).toMillis())
			}
			logger.error("全部可用bot下线")
			tasker = null
		}
	}
}
