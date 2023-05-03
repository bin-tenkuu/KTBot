package my.ktbot.plugs

import my.ktbot.PluginMain
import my.ktbot.annotation.Helper
import my.ktbot.annotation.NeedAdmin
import my.ktbot.annotation.SendAuto
import my.ktbot.database.TGroup
import my.ktbot.database.TMember
import my.ktbot.utils.Counter
import my.ktbot.utils.Sqlite
import my.ktbot.utils.calculator.Calculator
import my.ktbot.utils.toHelper
import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.emptyMessageChain
import net.mamoe.mirai.message.data.toPlainText
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.joinTo

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/13
 */
object CQBotListGet {
	@MiraiEventHandle("获取<type>列表")
	@RegexAnn("^.获取(?<type>[^ ]+)列表$")
	@NeedAdmin
	@SendAuto
	fun invoke(event: MessageEvent, groups: MatchGroupCollection): Message? {
		val type = groups["type"]?.value ?: return null
		when (type) {
			"群" -> return event.bot.groups.run {
				"总共 ${size} 个群:\n" + joinToString("\n") {
					"${it.id}: ${it.name}"
				}
			}.toPlainText()
			"好友" -> return event.bot.friends.run {
				"总共 ${size} 个好友:\n" + joinToString("\n") {
					"${it.id}: ${it.nick}"
				}
			}.toPlainText()
			"ban" -> return StringBuilder("群：\n").also { b ->
				Sqlite[TGroup].filter { it.isBaned eq true }.joinTo(b, "\n") { it.id.toString() }
			}.append("\n人：\n").also { b ->
				Sqlite[TMember].filter { it.isBaned eq true }.joinTo(b, "\n") { it.id.toString() }
			}.toString().toPlainText()
			else -> return null
		}
	}

	@MiraiEventHandle("插件[<id>]")
	@RegexAnn("^.插件(?<id> *\\d*)$")
	@NeedAdmin
	@Helper("查看插件信息")
	@SendAuto
	@JvmStatic
	private fun cqBotPluginInfo(groups: MatchGroupCollection): String {
		val list = PluginMain.callers
		val c = run {
			val num = groups["id"]?.run { value.trim().toIntOrNull() } ?: return@run null
			list.getOrNull(num)
		} ?: return list.mapIndexed { i, p ->
			"$i :${p.name}"
		}.joinToString("\n")
		return c.toHelper()
	}

	// @MessageHandle(".插件<open><nums[]>")
	// @RegexAnn("^.插件(?<open>[开关])(?<nums>[\\d ]+)$")
	// @NeedAdmin
	// @Helper("设置插件状态")
	// @SendAuto
	// @JvmStatic
	// private fun cqBotPluginStatus(result: MatchGroupCollection): Message? {
	// 	val isOpen = when (result["open"]!!.value) {
	// 		"开" -> true
	// 		"关" -> false
	// 		else -> return null
	// 	}
	// 	val ids = result["nums"]!!.value.split(" ").mapNotNull {
	// 		it.trim().toIntOrNull()
	// 	}.mapNotNull {
	// 		plugs.getOrNull(it)
	// 	}
	// 	if (ids.isEmpty()) {
	// 		return "未知插件ID".toPlainText()
	// 	}
	// 	return buildMessageChain {
	// 		+"插件变动:"
	// 		ids.forEach {
	// 			it.isOpen = isOpen
	// 			+"\n"
	// 			+it.name
	// 		}
	// 	}
	// }

	@MiraiEventHandle("日志")
	@RegexAnn("^.日志$")
	@NeedAdmin
	@SendAuto
	@JvmStatic
	private fun cqBotCounter(event: MessageEvent): Message {
		return Counter.state(event.subject)
	}
}
