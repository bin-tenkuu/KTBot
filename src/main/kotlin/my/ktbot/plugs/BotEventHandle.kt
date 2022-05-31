package my.ktbot.plugs

import kotlinx.coroutines.*
import my.ktbot.PlugConfig
import my.ktbot.PluginMain
import my.ktbot.utils.Counter
import my.ktbot.utils.update
import my.miraiplus.annotation.MessageHandle
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.isContentEmpty
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
