package my.ktbot.plugs

import my.ktbot.PluginPerm
import my.ktbot.PluginPerm.minusAssign
import my.ktbot.PluginPerm.plusAssign
import my.ktbot.interfaces.Plug
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText

object CQBotPerm : Plug(
	name = "添加删除权限",
	regex = Regex("^[.．。]权限(?<op>[^ ]{2})(?<groupid>\\d+)\$"),
	weight = 10.0,
	needAdmin = true,
	help = "添加/删除".toPlainText()
) {
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message? {
		val op = result["op"]?.value ?: return null
		val groupid = result["groupid"]?.value?.toLongOrNull() ?: return null
		return when (op) {
			"添加" -> {
				AbstractPermitteeId.ExactGroup(groupid) += PluginPerm.setu
				"群${groupid}添加setu权限成功".toPlainText()
			}
			"删除" -> {
				AbstractPermitteeId.ExactGroup(groupid) -= PluginPerm.setu
				"群${groupid}删除setu权限成功".toPlainText()
			}
			else -> return null
		}
	}
}
