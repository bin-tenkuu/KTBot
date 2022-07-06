package my.ktbot.plugs

import my.ktbot.PluginMain
import my.ktbot.annotation.*
import my.ktbot.database.TGroup
import my.ktbot.database.TMembers
import my.ktbot.utils.Counter
import my.ktbot.utils.Sqlite
import my.ktbot.utils.calculator.Calculator
import my.ktbot.utils.get
import my.miraiplus.annotation.MessageHandle
import my.miraiplus.annotation.RegexAnn
import my.miraiplus.annotation.RegexAnn.Companion.joinToString
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
	@MessageHandle(".获取<type>列表")
	@RegexAnn("^[.．。]获取(?<type>[^ ]+)列表$")
	@NeedAdmin
	@SendAuto
	fun invoke(event: MessageEvent, result: MatchResult): Message? {
		val type = result["type"]?.value ?: return null
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
				Sqlite[TMembers].filter { it.isBaned eq true }.joinTo(b, "\n") { it.id.toString() }
			}.toString().toPlainText()
			else -> return null
		}
	}

	@MessageHandle(".插件[<id>]")
	@RegexAnn("^[.．。]插件(?<id> *\\d*)$")
	@NeedAdmin
	@Helper("查看插件信息")
	@SendAuto
	@JvmStatic
	private fun cqBotPluginInfo(result: MatchResult): String {
		val list = PluginMain.callers
		val c = run {
			val num = result["id"]?.run { value.trim().toIntOrNull() } ?: return@run null
			list.getOrNull(num)
		} ?: return list.mapIndexed { i, p ->
			"$i :${p.name}"
		}.joinToString("\n")

		val sb = StringBuilder()
		sb.append("名称：").append(c.name)
		sb.append("\n事件类型：").append(c.eventClass.simpleName)
		for (ann in c.anns) {
			when (ann) {
				is Helper -> sb.append("\n帮助：").append(ann.help)
				is LimitAll -> sb.append("\n速度限制：").append(ann.time).append("毫秒/次")
				is NeedAdmin -> sb.append("\n<需要管理员>")
				is RegexAnn -> sb.append("\n正则匹配：").append(ann.pattern)
					.append("\n匹配规则：").append(ann.joinToString())
				is SendAuto -> sb.append("\n撤回延时：").append(ann.recall).append("\n<发送至上下文>")
				is SendAdmin -> sb.append("\n<发送至管理员>")
				is SendGroup -> sb.append("\n<发送至群聊>")
			}
		}
		return sb.toString()
		// return """
		// 	|帮助：${c.help}
		// 	|撤回延时：${c.deleteMSG}毫秒
		// 	|速度限制：${c.speedLimit}毫秒每次
		// """.trimMargin().toPlainText()
	}

	// @MessageHandle(".插件<open><nums[]>")
	// @RegexAnn("^[.．。]插件(?<open>[开关])(?<nums>[\\d ]+)$")
	// @NeedAdmin
	// @Helper("设置插件状态")
	// @SendAuto
	// @JvmStatic
	// private fun cqBotPluginStatus(result: MatchResult): Message? {
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

	@MessageHandle("日志")
	@RegexAnn("^[.．。]日志$")
	@NeedAdmin
	@SendAuto
	@JvmStatic
	private fun cqBotCounter(event: MessageEvent): Message {
		return Counter.state(event.subject)
	}

	@MessageHandle("简易计算器")
	@RegexAnn("^[.．。]calc (?<calc>[^ ]+)", RegexOption.IGNORE_CASE)
	@Helper("表达式间不允许出现空格")
	@SendAuto
	@JvmStatic
	private fun cqBotCalculate(result: MatchResult): Message {
		val calc = result["calc"]?.value ?: return emptyMessageChain()
		return try {
			"结果为${Calculator(calc).v}".toPlainText()
		}
		catch (e: Exception) {
			e.toString().toPlainText()
		}
	}
}
