package my.ktbot.plugs

import my.ktbot.annotation.SendAuto
import my.ktbot.annotation.Helper
import my.ktbot.annotation.NeedAdmin
import my.ktbot.interfaces.Plug
import my.ktbot.utils.get
import my.ktbot.utils.sendAdmin
import my.miraiplus.annotation.MessageHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/13
 */
object CQBotHelper {
	@MessageHandle(".(help|帮助)[<id>]")
	@RegexAnn("^[.．。](?:help|帮助)(?<num> ?\\d+)?$", RegexOption.IGNORE_CASE)
	@Helper("帮助专用功能\n.help后附带下标数字查看对应功能详情")
	@SendAuto(recall = 30 * 1000)
	fun invoke(/* event: MessageEvent, */ result: MatchResult): Message {
		/*
		kotlin.run {
			val list = PluginMain.myEventHandle.callers.filter {
				it.anns.any { ann -> ann is Helper }
					&& it.anns.none { ann -> ann is NeedAdmin }
			}
			event.sendAdmin(
				"发送下标\n${
					list.mapIndexed { i, c ->
						"$i :${c.name}"
					}.joinToString("\n")
				}"
			)
			val index = event.selectMessages {
				default {
					Regex("(\\d)").find(it)?.run { groupValues[0].toInt() } ?: 0
				}
				timeout(30 * 1000) {
					null as Int?
				}
			}
			if (index === null) return@run
			val caller = list.getOrNull(index) ?: return@run
			val s = caller.anns.filterIsInstance<Helper>().firstOrNull()?.help ?: return@run
			return s.toPlainText()
		}
		*/
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

	private fun get(): List<Plug> = Plug.plugs.filter { it.isOpen == true && !it.needAdmin && it.help !== null }

	@MessageHandle(".ping")
	@RegexAnn("^[.．。]ping$", RegexOption.IGNORE_CASE)
	@Helper("测试bot是否连接正常")
	@SendAuto
	private val Ping = ".pong!"

	@MessageHandle(".data")
	@RegexAnn("^[.．。]data$", RegexOption.IGNORE_CASE)
	@Helper("开发者信息")
	@SendAuto(recall = 90 * 1000)
	private val Data = """
		|开发者QQ：2938137849
		|项目地址github：2938137849/KTBot
		|轮子github：mamoe/mirai
	""".trimMargin()

	@MessageHandle(".report <txt>")
	@RegexAnn("^[.．。]report(?<txt>.+)$", RegexOption.IGNORE_CASE)
	@Helper("附上消息发送给开发者")
	@SendAuto
	@JvmStatic
	private suspend fun report(event: MessageEvent, result: MatchResult): String? {
		val txt = result["txt"]?.value ?: return null
		val group = if (event is GroupMessageEvent) "${event.group.name}(${event.group.id})" else ""
		event.sendAdmin("来自 ${group}${event.senderName}(${event.sender.id}):\n${txt}".toPlainText())
		return "收到"
	}

	@MessageHandle(".send[g]<qq> <txt>")
	@RegexAnn("^[.．。]send(?<g>g)?(?<qq>\\d+) (?<txt>.+)$", RegexOption.IGNORE_CASE)
	@NeedAdmin
	@Helper("bot代理发送消息")
	@SendAuto
	@JvmStatic
	private suspend fun sendMsg(event: MessageEvent, result: MatchResult): String {
		val qq = result["qq"]?.value?.toLongOrNull() ?: return "需要发送目标"
		val g = result["g"] !== null
		val txt = result["txt"]?.value ?: return "需要发送的消息"
		(if (g) event.bot.getGroup(qq) else event.bot.getFriend(qq))
			?.sendMessage(txt) ?: return "发送失败"
		return "已发送"
	}


}
