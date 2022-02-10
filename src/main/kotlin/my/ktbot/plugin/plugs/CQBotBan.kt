package my.ktbot.plugin.plugs

import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.utils.Counter
import my.ktbot.plugin.utils.update
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.toPlainText

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/11
 */
object CQBotBan : Plug(
	name = "设置ban状态",
	regex = Regex("^[.．。]设置(?<group>群)?(?<type>un)?ban(?<other>[ \\d]+)$"),
	weight = 4.0,
	needAdmin = true,
	help = "设置群聊、私聊的ban状态。格式：.设置[群][un]ban <other>".toPlainText()
) {
	private val empty = Regex(" +")
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message? {
		/**true为群聊，false为私聊*/
		val group = result["group"] !== null

		/**true为ban，false为unban*/
		val type = result["type"] === null
		val ids = (result["other"] ?: return null).value.split(empty).mapNotNull {
			it.toLongOrNull()
		}
		buildMessageChain {
			+"已"
			if (type) +"un"
			+"ban"
			+setBan(group, ids, type)
			+"：\n"
			ids.joinTo(this, "\n") { toString() }
		}
		return null
	}

	@JvmStatic
	private fun setBan(group: Boolean, ids: List<Long>, type: Boolean) = if (group) {
		ids.forEach { Counter.groups[it].update { isBaned = type } }
		"群"
	}
	else {
		ids.forEach { Counter.members[it].update { isBaned = type } }
		"QQ"
	}
}