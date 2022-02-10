package my.ktbot.plugin.plugs

import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.utils.Calculator
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText

/**
 *  @Date:2022/2/3
 *  @author bin
 *  @version 1.0.0
 */
object CQBotCalculate : Plug(
	name = "简易计算器",
	regex = Regex("^[.．。]calc (?<calc>[^ ]+)"),
	weight = 10.0,
	help = "表达式间不允许出现空格".toPlainText()
) {
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message {
		val calc = result["calc"]?.value ?: return EmptyMessageChain
		return try {
			"结果为${Calculator(calc).v}".toPlainText()
		} catch (e: Exception) {
			e.toString().toPlainText()
		}
	}
}