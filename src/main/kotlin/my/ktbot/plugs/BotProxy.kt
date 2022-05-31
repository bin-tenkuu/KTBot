package my.ktbot.plugs

import my.ktbot.annotation.AutoSend
import my.ktbot.utils.get
import my.miraiplus.annotation.MessageHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent

/**
 *  @Date:2022/5/31
 *  @author bin
 *  @version 1.0.0
 */
object BotProxy {
	private var groupId: Long? = null

	@MessageHandle(".c开始转发消息")
	@RegexAnn("^[.．。]cstart", RegexOption.IGNORE_CASE)
	@AutoSend
	suspend fun start(event: GroupMessageEvent): String {
		val gId = this.groupId
		if (gId !== null) {
			if (gId == event.subject.id) {
				return "尚未停止转发"
			}
			event.bot.getGroup(gId)?.sendMessage("停止转发")
		}
		this.groupId = event.subject.id
		return "开始转发消息至"
	}

	@MessageHandle(".c转发具体消息")
	@RegexAnn("^[.．。]c (?<msg>.*)", RegexOption.IGNORE_CASE)
	@AutoSend
	suspend fun cProxy(event: FriendMessageEvent, result: MatchResult): String {
		val gId = this.groupId ?: return "无具体转发群"
		val value = result["msg"]?.value ?: return "无匹配消息"
		val tmp = event.bot.getGroup(gId) ?: return "未找到对应群"
		tmp.sendMessage(value)
		return "转发至群：${tmp.name}(${gId})"
	}
}
