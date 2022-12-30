package my.ktbot.plugs

import my.ktbot.annotation.Helper
import my.ktbot.annotation.NeedAt
import my.ktbot.annotation.SendGroup
import my.ktbot.utils.CacheMap
import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.Qualifier
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import java.time.Duration

object CQBotWhoAtMe {
	private val tmp = HashMap<Long, CacheMap<Long, QuoteReply>>()

	@MiraiEventHandle("谁@我-记录", priority = EventPriority.MONITOR)
	@NeedAt(false)
	@JvmStatic
	private fun GroupMessageEvent.saveAt(@Qualifier("NeedAt") atList: List<At>) {
		if (atList.isEmpty()) {
			return
		}
		val map = tmp.getOrPut(group.id) { CacheMap(Duration.ofHours(24).toMillis()) }
		val quote = message.quote()
		for (at in atList) {
			map[at.target] = quote
		}
	}

	@MiraiEventHandle("谁@我")
	@RegexAnn("^谁(?:@|at)我$", RegexOption.IGNORE_CASE)
	@Helper("谁@我：当群内有@操作时记录24小时，仅记录最后一条@")
	@SendGroup
	@JvmStatic
	private fun GroupMessageEvent.whoAtMe(): Message? {
		val map = tmp[group.id] ?: return null
		if (map.size <= 0) {
			return null
		}
		val reply = map[sender.id] ?: return null
		return buildMessageChain {
			+reply
			+At(sender.id)
		}
	}
}
