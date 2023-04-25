package my.ktbot.plugs

import my.ktbot.PluginPerm
import my.ktbot.PluginPerm.minusAssign
import my.ktbot.PluginPerm.plusAssign
import my.ktbot.annotation.Helper
import my.ktbot.annotation.NeedAdmin
import my.ktbot.annotation.SendAuto
import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.emptyMessageChain
import net.mamoe.mirai.message.data.toPlainText

object CQBotPerm {
	@MiraiEventHandle("添加删除权限")
	@RegexAnn("^[.．。]权限(?<op>[^ ]{2})(?<groupid>\\d+)(?<permid>\\w+)$")
	@NeedAdmin
	@Helper("添加/删除")
	@SendAuto
	fun invoke(groups: MatchGroupCollection): Message {
		val op = groups["op"]?.value ?: return emptyMessageChain()
		val groupid = groups["groupid"]?.value?.toLongOrNull() ?: return emptyMessageChain()
		val permid = groups["permid"]?.value ?: return emptyMessageChain()
		val permission = PluginPerm[permid] ?: return PlainText("未知的权限ID: $permid")
		return when (op) {
			"添加" -> {
				permission += AbstractPermitteeId.ExactGroup(groupid)
				"群 $groupid 添加 $permid 权限成功".toPlainText()
			}
			"删除" -> {
				permission -= AbstractPermitteeId.ExactGroup(groupid)
				"群 $groupid 删除 $permid 权限成功".toPlainText()
			}
			else -> return emptyMessageChain()
		}
	}

}
