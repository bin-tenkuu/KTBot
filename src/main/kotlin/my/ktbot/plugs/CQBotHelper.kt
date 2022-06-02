package my.ktbot.plugs

import my.ktbot.PluginMain
import my.ktbot.annotation.*
import my.ktbot.utils.get
import my.ktbot.utils.sendAdmin
import my.miraiplus.Caller
import my.miraiplus.annotation.MessageHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
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
	fun invoke(result: MatchResult): String {
		val list = get()
		val c = run {
			val num = result["num"]?.run { value.trim().toIntOrNull() } ?: return@run null
			list.getOrNull(num)
		} ?: return ".help后附带下标数字查看对应功能详情${
			list.mapIndexed { i, p ->
				"\n$i :${p.name}"
			}.joinToString("")
		}"
		val sb = StringBuilder()
		sb.append("名称：").append(c.name)
		for (ann in c.anns) {
			when (ann) {
				is Helper -> sb.append("\n帮助：").append(ann.help)
				is LimitAll -> sb.append("\n速度限制：").append(ann.time).append("毫秒/次")
				is NeedAdmin -> sb.append("\n<需要管理员>")
				is RegexAnn -> sb.append("\n正则匹配：").append(ann.pattern).apply {
					ann.option.joinTo(sb, "、", "\n匹配规则：") {
						when (it) {
							RegexOption.IGNORE_CASE -> "忽略大小写"
							RegexOption.MULTILINE -> "多行文本"
							RegexOption.DOT_MATCHES_ALL -> "跨行匹配"
							else -> ""
						}
					}
				}
				is SendAuto -> sb.append("\n撤回延时：").append(ann.recall)
			}
		}
		return sb.toString()
	}

	private fun get(): List<Caller> = PluginMain.callers.filter {
		it.anns.any { ann -> ann is Helper } && it.anns.none { ann -> ann is NeedAdmin }
	}

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
