package my.ktbot.plugs

import my.ktbot.PluginPerm
import my.ktbot.PluginPerm.minusAssign
import my.ktbot.PluginPerm.plusAssign
import my.ktbot.annotation.Helper
import my.ktbot.annotation.NeedAdmin
import my.ktbot.annotation.SendAuto
import my.ktbot.utils.get
import my.miraiplus.annotation.MessageHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.message.data.*

object CQBotPerm {
	@MessageHandle("添加删除权限")
	@RegexAnn("^[.．。]权限(?<op>[^ ]{2})(?<groupid>\\d+)(?<permid>\\w+)$")
	@NeedAdmin
	@Helper("添加/删除")
	@SendAuto
	fun invoke(result: MatchResult): Message {
		val op = result["op"]?.value ?: return emptyMessageChain()
		val groupid = result["groupid"]?.value?.toLongOrNull() ?: return emptyMessageChain()
		val permid = result["permid"]?.value ?: return emptyMessageChain()
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
			else -> return emptyMessageChain()
		}
	}
}
