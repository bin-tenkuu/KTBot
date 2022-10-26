package my.ktbot.plugs

import my.ktbot.PluginMain
import my.ktbot.annotation.Helper
import my.ktbot.annotation.NeedAdmin
import my.ktbot.annotation.SendAuto
import my.ktbot.database.TJeffJoke
import my.ktbot.service.TodayInHistoryService
import my.ktbot.utils.*
import my.ktbot.utils.Sqlite.limit
import my.miraiplus.Caller
import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.Qualifier
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.sortedBy

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/13
 */
object CQBotHelper {
	private const val helpPre = ".help后附带下标数字查看对应功能详情"
	val callers: List<Caller> by lazy(LazyThreadSafetyMode.NONE) {
		PluginMain.callers.filter {
			it.anns.any { ann -> ann is Helper } && it.anns.none { ann -> ann is NeedAdmin }
		}
	}
	private val callerMap: Map<Int, Caller> by lazy(LazyThreadSafetyMode.NONE) {
		callers.mapIndexed { i, c -> i to c }.toMap()
	}

	@MiraiEventHandle("help")
	@RegexAnn("^[.．。](?:help|帮助) ?(?<num>\\d+)?(?<key>.+)?$", RegexOption.IGNORE_CASE)
	@Helper("帮助专用功能\n.help后附带下标数字查看对应功能详情")
	@SendAuto(recall = 30 * 1000)
	fun invoke(@Qualifier("num") numS: String?, @Qualifier("key") key: String?): String {
		val num = numS?.toIntOrNull()
		var map = callerMap
		if (num != null) {
			val c = map[num]
			if (c != null) {
				return c.toHelper()
			}
		}
		if (key != null) {
			map = map.filter { it.value.name.contains(key) }
			when (map.size) {
				0 -> return "没有这个关键字的功能"
				1 -> return map.values.iterator().next().toHelper()
			}
		}
		return "$helpPre${
			map.map { (i, p) ->
				"\n$i :${p.name}"
			}.joinToString("")
		}"
	}

	@MiraiEventHandle("ping")
	@RegexAnn("^[.．。]ping$", RegexOption.IGNORE_CASE)
	@Helper("测试bot是否连接正常")
	@SendAuto
	private val Ping = ".pong!"

	@MiraiEventHandle("开发者信息")
	@RegexAnn("^[.．。]data$", RegexOption.IGNORE_CASE)
	@Helper("开发者信息")
	@SendAuto(recall = 90 * 1000)
	private val Data = """
		|开发者QQ：2938137849
		|项目地址github：2938137849/KTBot
		|轮子github：mamoe/mirai
	""".trimMargin()

	@MiraiEventHandle("给开发者的话")
	@RegexAnn("^[.．。]report(?<txt>.+)$", RegexOption.IGNORE_CASE)
	@Helper("附上消息发送给开发者")
	@SendAuto
	@JvmStatic
	private suspend fun report(event: MessageEvent, groups: MatchGroupCollection): String? {
		val txt = groups["txt"]?.value ?: return null
		val group = if (event is GroupMessageEvent) "${event.group.name}(${event.group.id})" else ""
		event.sendAdmin("来自 ${group}${event.senderName}(${event.sender.id}):\n${txt}".toPlainText())
		return "收到"
	}

	@MiraiEventHandle("send[g]<qq> <txt>")
	@RegexAnn("^[.．。]send(?<g>g)? ?(?<qq>\\d+) (?<txt>.+)$", RegexOption.IGNORE_CASE)
	@NeedAdmin
	@Helper("bot代理发送消息")
	@SendAuto
	@JvmStatic
	private suspend fun sendMsg(event: MessageEvent, groups: MatchGroupCollection): String {
		val qq = groups["qq"]?.value?.toLongOrNull() ?: return "需要发送目标"
		val g = groups["g"] !== null
		val txt = groups["txt"]?.value ?: return "需要发送的消息"
		(if (g) event.bot.getGroup(qq) else event.bot.getFriend(qq))
			?.sendMessage(txt) ?: return "发送失败"
		return "已发送"
	}

	@MiraiEventHandle("系统信息")
	@RegexAnn("^[.．。]系统信息$")
	@NeedAdmin
	@Helper("系统信息")
	@SendAuto
	@JvmStatic
	private fun getSystemInfo(): String {
		return SystemInfoUtil()
	}

	@MiraiEventHandle("夸我")
	@RegexAnn("^[夸舔]我$")
	@Helper("彩虹屁")
	@SendAuto
	@JvmStatic
	private suspend fun rainbowFart(): String {
		return KtorUtils.rainbowFart()
	}

	@MiraiEventHandle("情话")
	@RegexAnn("^[.．。]情话$|^来点情话$")
	@Helper("y1s1，来点情话")
	@SendAuto
	@JvmStatic
	private suspend fun greenTea(): String {
		return KtorUtils.greenTea(false)
	}

	/**
	 * 来源 [https://mirai.mamoe.net/topic/1269/%E7%AE%80%E6%98%93jeff%E7%AC%91%E8%AF%9D%E7%94%9F%E6%88%90%E5%99%A8]
	 * @param event [MessageEvent]
	 * @param groups [MatchResult]
	 * @return [String]?
	 */
	@MiraiEventHandle("jeff笑话")
	@RegexAnn("^[.．。]joke(?<name> *.+)?$", RegexOption.IGNORE_CASE)
	@Helper("简易Jeff笑话生成，参数：<name> ：名字；<times>：次数")
	@SendAuto
	@JvmStatic
	private fun jeffJoke(event: MessageEvent, groups: MatchGroupCollection): String? {
		val joke = Sqlite[TJeffJoke].limit(1).sortedBy {
			Sqlite.random
		}.firstOrNull()?.text ?: return null
		val name = groups["name"]?.value?.trim() ?: event.senderName
		return joke.replace("%s", name)
	}

	/**
	 * 60秒读懂世界
	 * @param event MessageEvent
	 * @return Image
	 */
	@MiraiEventHandle("60秒读懂世界")
	@RegexAnn("^[.．。]60s(?<min> -txt)?\$", RegexOption.IGNORE_CASE)
	@Helper("60秒读懂世界")
	@SendAuto
	@JvmStatic
	private suspend fun read60s(event: MessageEvent, min: String?): Message {
		val user = event.sender
		if (min == null)
			return KtorUtils.read60s().uploadAsImage(event.subject)
		val day60s = KtorUtils.read60sJson()
		return ForwardMessageBuilder(event.subject, 20).apply {
			user says day60s.name
			user says day60s.time.joinToString(separator = " ")
			for (i in day60s.data.indices) {
				user says day60s.data[i]
			}
		}.build()
	}

	@MiraiEventHandle("历史上的今天")
	@RegexAnn("^历史上的今天\$", RegexOption.IGNORE_CASE)
	@Helper("历史上的今天")
	@SendAuto
	@JvmStatic
	private fun todayInHistory(): String {
		val history = TodayInHistoryService.getRandom().firstOrNull() ?: return ""
		return buildString {
			append(history.date)
			append("：")
			append(history.title)
			val content = TodayInHistoryService.getContent(history)
			if (content.isNotEmpty()) {
				append("\n")
				append(content)
			}
		}
	}

	private val word = Regex("^[0-9a-zA-Z]+\$")

	@MiraiEventHandle("能不能好好说话")
	@RegexAnn("^[?？](?<text>..+)\$", RegexOption.IGNORE_CASE)
	@Helper("能不能好好说话")
	@SendAuto
	@JvmStatic
	private suspend fun nbnhhsh(@Qualifier("text") text: String): String {
		return if (word.matches(text))
			KtorUtils.nbnhhsh(text).joinToString(", ", "$text: ")
		else ""
	}
}