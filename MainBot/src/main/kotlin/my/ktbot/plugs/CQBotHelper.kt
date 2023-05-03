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
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
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
	private val callers: List<Caller> by lazy(LazyThreadSafetyMode.NONE) {
		PluginMain.callers.filter {
			it.anns.any { ann -> ann is Helper } && it.anns.none { ann -> ann is NeedAdmin }
		}
	}
	private val callerMap: Map<Int, Caller> by lazy(LazyThreadSafetyMode.NONE) {
		callers.mapIndexed { i, c -> i to c }.toMap()
	}

	@MiraiEventHandle("help")
	@RegexAnn("^.(?:help|帮助) ?(\\d+)?(.+)?$", RegexOption.IGNORE_CASE)
	@Helper("帮助专用功能\n.help后附带下标数字查看对应功能详情")
	@SendAuto(recall = 30 * 1000)
	fun invoke(@Qualifier("1") numS: String?, @Qualifier("2") key: String?): String {
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

	@MiraiEventHandle("夸我")
	@RegexAnn("^[夸舔]我$")
	@Helper("彩虹屁")
	@SendAuto
	@JvmStatic
	private suspend fun rainbowFart(): String {
		return KtorUtils.rainbowFart()
	}

	@MiraiEventHandle("情话")
	@RegexAnn("^.情话$|^来点情话$")
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
	@RegexAnn("^.joke(?<name> *.+)?$", RegexOption.IGNORE_CASE)
	@Helper("简易Jeff笑话生成，参数：<name> ：名字；<times>：次数")
	@SendAuto
	@JvmStatic
	private fun jeffJoke(event: MessageEvent, groups: MatchGroupCollection): String? {
		val joke = Sqlite[TJeffJoke].limit(1).sortedBy {
			Sqlite.random()
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
	@RegexAnn("^.60s?\$", RegexOption.IGNORE_CASE)
	@Helper("60秒读懂世界")
	@SendAuto
	@JvmStatic
	private suspend fun read60s(event: MessageEvent): Message {
		val day60s = KtorUtils.read60sJson() ?: return "今日未更新".toPlainText()
		return buildMessageChain {
			+KtorUtils.pic(day60s.img).toExternalResource().toAutoCloseable().uploadAsImage(event.subject)
			for (text in day60s.texts) {
				+"\n"
				+text
			}
		}
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

	@MiraiEventHandle("能不能好好说话")
	@RegexAnn("^[?？]([0-9a-zA-Z]+)\$", RegexOption.IGNORE_CASE)
	@Helper("能不能好好说话")
	@SendAuto
	@JvmStatic
	private suspend fun nbnhhsh(@Qualifier("1") text: String): String {
		return KtorUtils.nbnhhsh(text).joinToString(", ", "$text: ")
	}
}
