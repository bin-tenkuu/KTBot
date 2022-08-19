package my.ktbot.plugs

import my.ktbot.annotation.SendAuto
import my.miraiplus.annotation.MiraiEventHandle
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
	private val space = Regex("[　\\s]+")
	private val toSpace = "：:。，.,！!？?、；; ".toSet()
	private val toDel = "“”\"【】《》「」".toSet()

	@MiraiEventHandle("c 开始转发消息")
	@RegexAnn("^[.．。]cstart", RegexOption.IGNORE_CASE)
	@SendAuto
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

	@MiraiEventHandle("c 转发具体消息")
	@RegexAnn("^[.．。]c (?<msg>.*)", RegexOption.IGNORE_CASE, RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
	@SendAuto
	suspend fun cProxy(event: FriendMessageEvent, groups: MatchGroupCollection): String {
		val gId = this.groupId ?: return "无具体转发群"
		val tmp = event.bot.getGroup(gId) ?: return "未找到对应群"
		val value = groups["msg"]?.value ?: return "无匹配消息"
		val msg = String(value.mapNotNull {
			if (it in toSpace) return@mapNotNull '　'
			else if (it in toDel) return@mapNotNull null
			return@mapNotNull it
		}.toCharArray()).replace(space, "　").trim()
		if (msg.isNotEmpty()) {
			tmp.sendMessage(msg)
		}
		return "转发至群：${tmp.name}(${gId})"
	}
}
