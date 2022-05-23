package my.ktbot.plugs

import kotlinx.coroutines.launch
import my.ktbot.PlugConfig
import my.ktbot.annotation.AutoCall
import my.ktbot.annotation.MsgLength
import my.ktbot.annotation.RegexAnn
import my.ktbot.interfaces.Plug
import my.ktbot.utils.NginxLogHandle
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CQNginxLogHandle : Plug(
	name = "nginx日志处理",
	regex = Regex("^[.．。]nginx$", RegexOption.IGNORE_CASE),
	weight = 10.0,
	needAdmin = true,
	canPrivate = false,
	canGroup = true
) {
	private val formater = DateTimeFormatter.ofPattern("uuuu-MM-dd HH-mm")
	private val nowFile: String get() = formater.format(LocalDateTime.now())
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message {
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
		Regex("((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])")

	@AutoCall(
		name = "添加banip",
		regex = RegexAnn("^[.．。]banip(.+)$", RegexOption.IGNORE_CASE),
		weight = 10.0,
		msgLength = MsgLength(0, Int.MAX_VALUE),
		needAdmin = true
	)
	private fun addIP(result: MatchResult): String {
		val s = result.groups[1]?.value ?: return "未匹配到ip"
		val list = ipRegex.findAll(s).map(MatchResult::value).toMutableList()
		if (list.isEmpty()) {
			return "未匹配到ip"
		}
		NginxLogHandle.banip(list)
		return NginxLogHandle.banipResult().joinToString("\n")
	}
}
