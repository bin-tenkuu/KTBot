package my.ktbot.plugs

import my.ktbot.interfaces.Plug
import my.ktbot.utils.CacheMap
import my.ktbot.utils.Counter
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import java.time.Duration

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/11
 */
object MemberExp : Plug(
	name = "qq活跃",
	regex = Regex("^[.．。]state(?<qq> ?\\d{5,12})?$", RegexOption.IGNORE_CASE),
	weight = 6.0,
	help = "查看自己/<qq>的信息,群聊限时1次/分钟".toPlainText(),
	hidden = true,
) {
	@JvmStatic
	private val cache = CacheMap<Long, MutableSet<Long>>(Duration.ofMinutes(1).toMillis())
	override suspend fun invoke(event: FriendMessageEvent, result: MatchResult): Message {
		val qq = result["qq"]?.value?.toLongOrNull() ?: event.sender.id
		cache.getOrInit(event.sender.id) { mutableSetOf(qq) }
		val exp = Counter.members[qq].exp
		return buildMessageChain {
			+At(event.sender)
			+"$exp"
		}
	}

	override suspend fun invoke(event: GroupMessageEvent, result: MatchResult): Message? {
		val qq = result["qq"]?.value?.toLongOrNull() ?: event.sender.id
		if (cache[event.sender.id]?.contains(qq) == true) {
			return null
		}
		return super.invoke(event, result)
	}
}
