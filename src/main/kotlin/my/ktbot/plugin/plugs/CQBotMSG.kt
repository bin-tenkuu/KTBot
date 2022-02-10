package my.ktbot.plugin.plugs

import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.utils.sendAdmin
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/14
 */
object CQBotMSG {
	private val Ping = Plug.StringPlug(
		name = ".ping",
		regex = Regex("^[.．。]ping$", RegexOption.IGNORE_CASE),
		weight = 0.0,
		help = "测试bot是否连接正常".toPlainText(),
		msgLength = 4..6,
		msg = ".pong!".toPlainText()
	)

	private val Data = Plug.StringPlug(
		name = ".data",
		regex = Regex("^[.．。]data$", RegexOption.IGNORE_CASE),
		weight = 10.0,
		help = "开发者信息".toPlainText(),
		deleteMSG = 90 * 1000,
		msgLength = 4..6,
		msg = """
			|开发者QQ：2938137849
			|项目地址github：2938137849/KTBot
			|轮子github：mamoe/mirai
		""".trimMargin().toPlainText()
	)

	@JvmStatic
	val list = arrayOf(
		Ping, Data,
		Report
	)

	private object Report : Plug(
		name = ".report <txt>",
		regex = Regex("^[.．。]report(?<txt>.+)$", RegexOption.IGNORE_CASE),
		weight = 6.0,
		help = "附上消息发送给开发者".toPlainText()
	) {
		override suspend fun invoke(event: MessageEvent, result: MatchResult): Message? {
			val txt = result["txt"]?.value ?: return null
			event.sendAdmin("来自 ${event.senderName}(${event.sender.id}):\n${txt}".toPlainText())
			return "收到".toPlainText()
		}
	}

}