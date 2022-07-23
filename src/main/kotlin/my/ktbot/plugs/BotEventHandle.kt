package my.ktbot.plugs

import kotlinx.coroutines.*
import my.ktbot.PlugConfig
import my.ktbot.PluginMain
import my.ktbot.annotation.SendAdmin
import my.ktbot.annotation.SendGroup
import my.ktbot.database.Gmt.Companion.update
import my.ktbot.utils.CacheMap
import my.ktbot.utils.Counter
import my.miraiplus.annotation.MessageHandle
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiLogger
import java.time.Duration

/**
 *  @Date:2022/5/31
 *  @author bin
 *  @version 1.0.0
 */
object BotEventHandle {
	private val logger = MiraiLogger.Factory.create(BotEventHandle::class)

	@JvmStatic
	private var tasker: Job? = null

	@MessageHandle("Bot 上线事件")
	private fun BotOnlineEvent.run() {
		startCounter()
		// 开启所有列表缓存
		bot.configuration.enableContactCache()
		logger.info("${bot.nameCardOrNick} 已上线")
	}

	@MessageHandle("Bot 下线事件")
	private fun BotOfflineEvent.run() {
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

	@MessageHandle("Bot 重新登录事件")
	private fun BotReloginEvent.run() {
		logger.info("${bot.nameCardOrNick} 已重新登录")
	}

	private val inviteCount = CacheMap<Long, Unit>(Duration.ofHours(12).toMillis())

	@MessageHandle("添加好友事件")
	@SendAdmin
	private suspend fun NewFriendRequestEvent.run(): String {
		val msg = "${fromNick}（${fromId}）来自群 ${fromGroup?.name ?: ""}（${fromGroupId}）请求添加好友消息：\n${message}"
		logger.info("NewFriendRequestEvent: ${msg}")
		if (inviteCount.size <= 10) {
			inviteCount[fromId] = Unit
			//自动同意好友申请
			accept()
		}
		else reject()
		return msg
	}

	@MessageHandle("邀请加入群事件")
	@SendAdmin
	private fun BotInvitedJoinGroupRequestEvent.run(): String {
		val msg = "${invitorNick}（${invitorId}）邀请加入群 ${groupName}（${groupId}）"
		logger.info("BotInvitedJoinGroupRequestEvent: ${msg}")
		Counter.groups[groupId].update {
			exp = 20.0
			invited = invitorId
		}
//		if (inviteCount.size <= 10) {
//			inviteCount[groupId] = Unit
//			//自动同意加群申请
//			accept()
//		}
		return msg
	}

	@MessageHandle("成员加入群事件")
	@SendGroup
	private fun MemberJoinEvent.run(): String {
		val msg = "@${member.nick} ${
			when (this) {
				is MemberJoinEvent.Invite -> "被 @${invitor.nick} 邀请"
				is MemberJoinEvent.Active -> "欢迎"
				is MemberJoinEvent.Retrieve -> "恢复群主身份"
				else -> ""
			}
		}入群"
		logger.info("MemberJoinEvent: ${msg}")
		return msg
	}

	@OptIn(MiraiExperimentalApi::class)
	@MessageHandle("Bot 加入群事件")
	@SendAdmin
	private fun BotJoinGroupEvent.run(): String {
		val msg = "bot成功加入群： ${group.name}(${groupId}), 来源：${
			when (this) {
				is BotJoinGroupEvent.Invite -> "邀请人： ${invitor.nick}(${invitor.id})"
				is BotJoinGroupEvent.Active -> "不确定"
				is BotJoinGroupEvent.Retrieve -> "恢复群主身份"
				else -> ""
			}
		}"
		logger.info("BotJoinGroupEvent: ${msg}")
		return msg
	}

	@MessageHandle("成员离开群事件")
	@SendGroup
	private fun MemberLeaveEvent.run(): String {
		val msg = "@${member.nick}(${member.id})${
			when (this) {
				is MemberLeaveEvent.Quit -> "主动离开本群"
				is MemberLeaveEvent.Kick -> "被 管理员(@${operator?.nick ?: bot.nick}) 踢出本群"
				else -> "未知方式离开本群"
			}
		}"
		logger.info("MemberLeaveEvent: ${msg}")
		return msg
	}

	@OptIn(MiraiExperimentalApi::class)
	@MessageHandle("Bot 离开群事件")
	@SendAdmin
	private fun BotLeaveEvent.run(): String {
		val msg = "bot被踢出群：${groupId}(${group.name}), 原因：${
			when (this) {
				is BotLeaveEvent.Active -> "主动退出, 有被踢出可能"
				is BotLeaveEvent.Kick -> "被踢出群"
				is BotLeaveEvent.Disband -> "群主解散群聊"
				else -> ""
			}
		}"
		logger.info("BotLeaveEvent: ${msg}")
		return msg
	}

	@MessageHandle("其他客户端上线事件")
	@SendAdmin
	private fun OtherClientOnlineEvent.run(): String {
		val msg = """其他客户端上线
			|设备名称:${client.info.deviceName}
			|设备类型:${client.info.deviceKind}
		""".trimMargin()
		logger.info("OtherClientOnlineEvent: ${msg}")
		return msg
	}

	@MessageHandle("其他客户端上线事件")
	@SendAdmin
	private fun OtherClientOfflineEvent.run(): String {
		val msg = """其他客户端下线
			|设备名称:${client.info.deviceName}
			|设备类型:${client.info.deviceKind}
		""".trimMargin()
		logger.info("OtherClientOnlineEvent: ${msg}")
		return msg
	}

	@MessageHandle("Bot 被禁言事件")
	private fun BotMuteEvent.run() {
		val msg = "bot被禁言，群: ${group.name}(${groupId}), 操作人: ${operator.nameCardOrNick}(${operator.id})"
		logger.info("BotMuteEvent: ${msg}")
		Counter.groups[groupId].update { isBaned = true }
	}

	@MessageHandle("Bot 被取消禁言事件")
	private fun BotUnmuteEvent.run() {
		val msg = "bot被取消禁言，群: ${group.name}(${groupId}), 操作人: ${operator.nameCardOrNick}(${operator.id})"
		logger.info("BotUnmuteEvent: ${msg}")
		Counter.groups[groupId].update { isBaned = false }
	}

	@JvmStatic
	private fun startCounter() {
		tasker?.cancel()
		if (Bot.instances.isEmpty()) {
			logger.error("无可用bot")
			tasker = null
			return
		}
		tasker = PluginMain.launch(PluginMain.coroutineContext, CoroutineStart.UNDISPATCHED) {
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
