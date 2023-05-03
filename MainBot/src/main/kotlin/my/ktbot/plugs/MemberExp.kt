package my.ktbot.plugs

import my.ktbot.annotation.Helper
import my.ktbot.annotation.NeedExp
import my.ktbot.annotation.SendAuto
import my.ktbot.utils.CacheMap
import my.ktbot.utils.Counter
import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*
import java.time.Duration

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/11
 */
object MemberExp {
	@JvmStatic
	private val cache = CacheMap<Long, MutableSet<Long>>(Duration.ofMinutes(1).toMillis())

	@MiraiEventHandle("经验增加", priority = EventPriority.MONITOR)
	@NeedExp(0.5, 1.0)
	@JvmStatic
	private fun invoke() {
	}

	@MiraiEventHandle("qq活跃")
	@RegexAnn("^.state(?<qq> ?\\d{5,12})?$", RegexOption.IGNORE_CASE)
	@Helper("查看自己/<qq>的信息，群聊限时1次/分钟")
	@SendAuto
	fun invoke(event: MessageEvent, groups: MatchGroupCollection): Message {
		val qq = groups["qq"]?.value?.toLongOrNull() ?: event.sender.id
		if (!cache.getOrInit(event.subject.id, ::HashSet).add(qq) && event is GroupMessageEvent) {
			return emptyMessageChain()
		}
		val exp = Counter.members[qq].exp
		return buildMessageChain {
			+At(event.sender)
			+"$exp"
		}
	}
}
