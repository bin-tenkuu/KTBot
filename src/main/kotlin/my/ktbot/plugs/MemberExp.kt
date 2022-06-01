package my.ktbot.plugs

import my.ktbot.annotation.SendAuto
import my.ktbot.annotation.Helper
import my.ktbot.utils.CacheMap
import my.ktbot.utils.Counter
import my.ktbot.utils.get
import my.miraiplus.annotation.MessageHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.event.events.FriendMessageEvent
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

	@MessageHandle("qq活跃(好友)")
	@RegexAnn("^[.．。]state(?<qq> ?\\d{5,12})?$", RegexOption.IGNORE_CASE)
	@Helper("查看自己/<qq>的信息")
	@SendAuto
	fun invoke(event: FriendMessageEvent, result: MatchResult): Message {
		return invoke(event, result, false)
	}

	fun invoke(event: MessageEvent, result: MatchResult, limit: Boolean): Message {
		val qq = result["qq"]?.value?.toLongOrNull() ?: event.sender.id
		if (!cache.getOrInit(event.subject.id, ::HashSet).add(qq) && limit) {
			return EmptyMessageChain
		}
		val exp = Counter.members[qq].exp
		return buildMessageChain {
			+At(event.sender)
			+"$exp"
		}
	}

	@MessageHandle("qq活跃(群聊)")
	@RegexAnn("^[.．。]state(?<qq> ?\\d{5,12})?$", RegexOption.IGNORE_CASE)
	@Helper("查看自己/<qq>的信息,限时1次/分钟")
	@SendAuto
	fun invoke(event: GroupMessageEvent, result: MatchResult): Message {
		return invoke(event as MessageEvent, result, true)
	}
}
