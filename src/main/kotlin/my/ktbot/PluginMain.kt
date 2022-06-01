package my.ktbot

import kotlinx.coroutines.CompletableJob
import my.ktbot.annotation.*
import my.ktbot.interfaces.Plug
import my.ktbot.plugs.*
import my.ktbot.utils.*
import my.miraiplus.MyEventHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.PlainText
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
		subEvents()
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

	private fun subEvents() {
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
