package my.ktbot.plugin.plugs

import my.ktbot.plugin.annotation.Plug
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote

/**
 *  @Date:2022/1/8
 *  @author bin
 *  @version 1.0.0
 */
object MemeAI : Plug(
	name = "(@复读AI)",
	regex = Regex("^"),
	weight = 91.0,
	msgLength = 0..50,
	canPrivate = false,
) {

	@JvmStatic
	private val memeList = arrayOf(
		Regex("(?<!\\\\)不") to "\\很",
		Regex("(?<!\\\\)你") to "\\我",
		Regex("(?<!\\\\)我") to "\\你",
		Regex("(?<![没\\\\])有") to "\\没有",
		Regex("(?<!\\\\)没有") to "\\有",
		Regex("[？?]") to "!",
		Regex("[\\\\吗]") to "",
	)

	override suspend fun invoke(event: GroupMessageEvent, result: MatchResult): Message? {
		if (!event.message.contains(At(event.bot.id))) return null
		val msg: String = memeList.fold(event.message.filterIsInstance<PlainText>().joinToString("") {
			it.contentToString()
		}) { str, (r, s) ->
			r.replace(str, s)
		}
		return buildMessageChain {
			+event.message.quote()
			+event.sender.at()
			+msg
		}
	}
}