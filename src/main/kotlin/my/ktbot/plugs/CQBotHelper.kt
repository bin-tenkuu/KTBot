package my.ktbot.plugs

import my.ktbot.annotation.AutoCall
import my.ktbot.annotation.MsgLength
import my.ktbot.annotation.RegexAnn
import my.ktbot.interfaces.Plug
import my.ktbot.utils.sendAdmin
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

	private fun get(): List<Plug> = plugs.filter { it.isOpen == true && !it.needAdmin && it.help !== null }

	@AutoCall(
		name = ".ping",
		regex = RegexAnn("^[.．。]ping$", RegexOption.IGNORE_CASE),
		weight = 0.0,
		help = "测试bot是否连接正常",
		msgLength = MsgLength(4, 6),
	)
	private val Ping = ".pong!"

	@AutoCall(
		name = ".data",
		regex = RegexAnn("^[.．。]data$", RegexOption.IGNORE_CASE),
		weight = 10.0,
		help = "开发者信息",
		deleteMSG = 90 * 1000,
		msgLength = MsgLength(4, 6),
	)
	private val Data = """
		|开发者QQ：2938137849
		|项目地址github：2938137849/KTBot
		|轮子github：mamoe/mirai
	""".trimMargin()

	@AutoCall(
		name = ".report <txt>",
		regex = RegexAnn("^[.．。]report(?<txt>.+)$", RegexOption.IGNORE_CASE),
		weight = 6.0,
		help = "附上消息发送给开发者"
	)
	private suspend fun report(event: MessageEvent, result: MatchResult): String? {
		val txt = result["txt"]?.value ?: return null
		event.sendAdmin("来自 ${event.senderName}(${event.sender.id}):\n${txt}".toPlainText())
		return "收到"
	}


}
