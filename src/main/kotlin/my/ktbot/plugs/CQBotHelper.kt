package my.ktbot.plugs

import my.ktbot.PluginMain
import my.ktbot.annotation.*
import my.ktbot.database.TJeffJoke
import my.ktbot.utils.*
import my.ktbot.utils.Sqlite.limit
import my.miraiplus.Caller
import my.miraiplus.annotation.MessageHandle
import my.miraiplus.annotation.RegexAnn
import my.miraiplus.annotation.RegexAnn.Companion.joinToString
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.toPlainText
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.sortedBy

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/13
 */
object CQBotHelper {
	@MessageHandle("help[<id>]")
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
				is RegexAnn -> sb.append("\n正则匹配：").append(ann.pattern)
					.append("\n匹配规则：").append(ann.joinToString())
				is SendAuto -> sb.append("\n撤回延时：").append(ann.recall)
			}
		}
		return sb.toString()
	}

	private fun get(): List<Caller> = PluginMain.callers.filter {
		it.anns.any { ann -> ann is Helper } && it.anns.none { ann -> ann is NeedAdmin }
	}

	@MessageHandle("ping")
	@RegexAnn("^[.．。]ping$", RegexOption.IGNORE_CASE)
	@Helper("测试bot是否连接正常")
	@SendAuto
	private val Ping = ".pong!"

	@MessageHandle("data")
	@RegexAnn("^[.．。]data$", RegexOption.IGNORE_CASE)
	@Helper("开发者信息")
	@SendAuto(recall = 90 * 1000)
	private val Data = """
		|开发者QQ：2938137849
		|项目地址github：2938137849/KTBot
		|轮子github：mamoe/mirai
	""".trimMargin()

	@MessageHandle("report <txt>")
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

	@MessageHandle("send[g]<qq> <txt>")
	@RegexAnn("^[.．。]send(?<g>g)? ?(?<qq>\\d+) (?<txt>.+)$", RegexOption.IGNORE_CASE)
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

	@MessageHandle("夸我")
	@RegexAnn("^[夸舔]我$")
	@Helper("彩虹屁")
	@SendAuto
	@JvmStatic
	private suspend fun rainbowFart(): String {
		return KtorUtils.rainbowFart()
	}

	/**
	 * 来源 [https://mirai.mamoe.net/topic/1269/%E7%AE%80%E6%98%93jeff%E7%AC%91%E8%AF%9D%E7%94%9F%E6%88%90%E5%99%A8]
	 * @param event [MessageEvent]
	 * @param result [MatchResult]
	 * @return [String]?
	 */
	@MessageHandle("jeffJoke")
	@RegexAnn("^[.．。]joke(?<name> *.+)?$", RegexOption.IGNORE_CASE)
	@Helper("简易Jeff笑话生成，参数：<name> ：名字；<times>：次数")
	@SendAuto
	@JvmStatic
	private fun jeffJoke(event: MessageEvent, result: MatchResult): String? {
		val joke = Sqlite[TJeffJoke].limit(1).sortedBy {
			Sqlite.random
		}.firstOrNull()?.text ?: return null
		val name = result["name"]?.value?.trim() ?: event.senderName
		return joke.replace("%s", name)
	}
}
