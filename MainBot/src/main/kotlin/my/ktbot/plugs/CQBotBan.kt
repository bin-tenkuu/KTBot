package my.ktbot.plugs

import my.ktbot.annotation.Helper
import my.ktbot.annotation.NeedAdmin
import my.ktbot.annotation.SendAuto
import my.ktbot.database.Gmt.Companion.update
import my.ktbot.utils.Counter
import my.miraiplus.annotation.MiraiEventHandle
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

	@MiraiEventHandle("设置ban状态")
	@RegexAnn("^.设置(?<group>群)?(?<type>un)?ban(?<other>[ \\d]+)$", RegexOption.IGNORE_CASE)
	@NeedAdmin
	@Helper("设置群聊、私聊的ban状态。格式：.设置[群][un]ban <other>")
	@SendAuto
	private fun invoke(groups: MatchGroupCollection): Message? {
		/**true为群聊，false为私聊*/
		val group = groups["group"] !== null

		/**true为ban，false为unban*/
		val type = groups["type"] === null
		val ids = (groups["other"] ?: return null).value.split(empty).mapNotNull { it.toLongOrNull() }
		return buildMessageChain {
			+"已"
			if (type) +"un"
			+"ban"
			+setBan(group, ids, type)
			+"：\n"
			ids.joinTo(this, "\n") { toString() }
		}
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
