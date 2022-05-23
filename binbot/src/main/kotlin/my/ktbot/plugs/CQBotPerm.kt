package my.ktbot.plugs

import my.ktbot.PluginPerm
import my.ktbot.PluginPerm.minusAssign
import my.ktbot.PluginPerm.plusAssign
import my.ktbot.interfaces.Plug
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*

object CQBotPerm : Plug(
	name = "添加删除权限",
	regex = Regex("^[.．。]权限(?<op>[^ ]{2})(?<groupid>\\d+)(?<permid>\\w+)$"),
	weight = 10.0,
	needAdmin = true,
	help = "添加/删除".toPlainText()
) {
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message {
		val op = result["op"]?.value ?: return EmptyMessageChain
		val groupid = result["groupid"]?.value?.toLongOrNull() ?: return EmptyMessageChain
		val permid = result["permid"]?.value ?: return EmptyMessageChain
		val permission = PluginPerm[permid] ?: return PlainText("未知的权限ID: ${permid}")
		return when (op) {
			"添加" -> {
				permission += AbstractPermitteeId.ExactGroup(groupid)
				"群 ${groupid} 添加 ${permid} 权限成功".toPlainText()
			}
			"删除" -> {
				permission -= AbstractPermitteeId.ExactGroup(groupid)
				"群 ${groupid} 删除 ${permid} 权限成功".toPlainText()
			}
			else -> return EmptyMessageChain
		}
	}
}
