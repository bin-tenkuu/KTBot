package my.ktbot.plugs

import my.ktbot.interfaces.Plug
import my.ktbot.utils.ReplaceNode
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/14
 */
object CQBotMemeAI : Plug(
	name = "(@复读AI)",
	regex = Regex("^"),
	weight = 91.0,
	msgLength = 0..50,
	hidden = true,
	canPrivate = false,
) {

	@JvmStatic
	private val replaceNode = ReplaceNode() + mapOf(
		"不" to "很",
		"你" to "我",
		"我" to "你",
		"有" to "没有",
		"没有" to "有",
		"有没有" to "肯定有",
		"是" to "不是",
		"不是" to "是",
		"是不是" to "肯定是",
		"？" to "!",
		"?" to "!",
		"吗" to "",
	)

	override suspend fun invoke(event: GroupMessageEvent, result: MatchResult): Message? {
		if (!event.message.contains(At(event.bot.id))) return null
		val msg: String = replaceNode.replace(
			event.message.filterIsInstance<PlainText>()
				.joinToString("", transform = PlainText::contentToString)
		)
		return buildMessageChain {
			+event.message.quote()
			+event.sender.at()
			+msg
		}
	}

}
