package my.ktbot.plugs

import my.ktbot.PluginPerm
import my.ktbot.PluginPerm.minusAssign
import my.ktbot.PluginPerm.plusAssign
import my.ktbot.annotation.*
import my.ktbot.utils.get
import my.ktbot.utils.toHelper
import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*

object CQBotPerm {
	@MiraiEventHandle("添加删除权限")
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

	@MiraiEventHandle("群管理员控制群内事件")
	@RegexAnn("^[.．。]功能(?<type>[开关]) ?(?<num>\\d+)$")
	@Helper("群管理员控制群内事件。使用例：。功能关 0\n注意不要把这个功能关了，不然没法开")
	@SendAuto
	fun groupAdminPerm(event: GroupMessageEvent, result: MatchResult): String? {
		// 普通成员
		if (event.sender.permission == MemberPermission.MEMBER) return null
		// 管理或者群主
		val num = result["num"]?.value?.toIntOrNull() ?: return "输入的不是数字"
		val caller = CQBotHelper.get().getOrNull(num) ?: return "未找到对应插件，使用。help 查看对应插件的id"
		val type = result["type"]?.value != "关"
		if (type) CheckPerm.open(event.group.id, caller.name)
		else CheckPerm.close(event.group.id, caller.name)
		return buildString {
			append(if (type) "打开" else "关闭")
			append("功能：\n")
			append(caller.toHelper())
		}
	}
}
