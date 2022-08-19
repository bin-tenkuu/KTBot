package my.ktbot.plugs

import kotlinx.coroutines.launch
import my.ktbot.PlugConfig
import my.ktbot.annotation.NeedAdmin
import my.ktbot.annotation.SendAuto
import my.ktbot.utils.NginxLogHandle
import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CQNginxLogHandle {
	private val formater = DateTimeFormatter.ofPattern("uuuu-MM-dd HH-mm")
	private val nowFile: String get() = formater.format(LocalDateTime.now())

	@MiraiEventHandle("nginx日志处理")
	@RegexAnn("^[.．。]nginx$", RegexOption.IGNORE_CASE)
	@NeedAdmin
	@SendAuto
	fun invoke(event: GroupMessageEvent): Message {
		if (NginxLogHandle.logSize() < 1) {
			return "无日志".toPlainText()
		}
		val sb = StringBuilder("正在发送")
		for (it in NginxLogHandle.banipResult()) {
			sb.append("\n").append(it)
		}
		val group = PlugConfig.getAdminGroup(event.bot)
		group.launch {
			val csv = NginxLogHandle.log2csv().toByteArray().toExternalResource(".csv").toAutoCloseable()
			group.files.uploadNewFile("$nowFile.csv", csv)
			val yml = NginxLogHandle.log2Yml().toByteArray().toExternalResource(".yml").toAutoCloseable()
			group.files.uploadNewFile("$nowFile.yml", yml)
			NginxLogHandle.flushFiles()
		}
		return PlainText(sb)
	}

	private val ipRegex =
		Regex("""((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.){3}(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])""")

	@MiraiEventHandle("添加banip")
	@RegexAnn("^[.．。]banip(.+)$", RegexOption.IGNORE_CASE)
	@SendAuto
	@NeedAdmin
	private fun addIP(groups: MatchGroupCollection): String {
		val s = groups[1]?.value ?: return "未匹配到ip"
		val list = ipRegex.findAll(s).map { it.value }.toMutableList()
		if (list.isEmpty()) {
			return "未匹配到ip"
		}
		NginxLogHandle.banip(list)
		return NginxLogHandle.banipResult().joinToString("\n")
	}
}
