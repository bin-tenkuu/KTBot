package my.ktbot.plugs

import my.ktbot.annotation.SendAuto
import my.ktbot.annotation.Helper
import my.ktbot.annotation.NeedAdmin
import my.ktbot.utils.Counter
import my.ktbot.utils.get
import my.ktbot.utils.update
import my.miraiplus.annotation.MessageHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.buildMessageChain

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/11
 */
object CQBotBan {
	@JvmStatic
	private val empty = Regex(" +")

	@MessageHandle("设置ban状态")
	@RegexAnn("^[.．。]设置(?<group>群)?(?<type>un)?ban(?<other>[ \\d]+)$", RegexOption.IGNORE_CASE)
	@NeedAdmin
	@Helper("设置群聊、私聊的ban状态。格式：.设置[群][un]ban <other>")
	@SendAuto
	private fun invoke(result: MatchResult): Message? {
		/**true为群聊，false为私聊*/
		val group = result["group"] !== null

		/**true为ban，false为unban*/
		val type = result["type"] === null
		val ids = (result["other"] ?: return null).value.split(empty).mapNotNull(String::toLongOrNull)
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
