package my.ktbot.plugs

import my.ktbot.interfaces.Plug
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/13
 */
object CQBotHelper : Plug(
	name = ".(help|帮助)[<id>]",
	regex = Regex("^[.．。](?:help|帮助)(?<num> ?\\d+)?$", RegexOption.IGNORE_CASE),
	weight = 2.0,
	deleteMSG = 30 * 1000,
	msgLength = 3..10,
	help = """帮助专用功能
		|.help后附带下标数字查看对应功能详情
	""".trimMargin().toPlainText()
) {
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message {
		val plugs = this.get()
		val p = run {
			val num = result["num"]?.run { value.trim().toIntOrNull() } ?: return@run null
			plugs.getOrNull(num)
		} ?: return """
			|.help后附带下标数字查看对应功能详情
			|${
			plugs.mapIndexed { i, p ->
				"$i :${p.name}"
			}.joinToString("\n")
		}""".trimMargin().toPlainText()
		return """
			|名称：${p.name}
			|匹配：${p.regex}
			|长度限制：${p.msgLength}
			|撤回延时：${p.deleteMSG}毫秒
			|速度限制：${p.speedLimit}毫秒每次
			|帮助：
		""".trimMargin().toPlainText() + p.help!!
	}

	private fun get(): List<Plug> =
		plugs.filter { it.isOpen == true && !it.needAdmin && it.help !== null }
}
