package my.ktbot.plugs

import kotlinx.coroutines.launch
import my.ktbot.PlugConfig
import my.ktbot.annotation.AutoCall
import my.ktbot.annotation.RegexAnn
import my.ktbot.interfaces.Plug
import my.ktbot.utils.NginxLogHandle
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CQNginxLogHandle : Plug(
	name = "nginx日志处理",
	regex = Regex("^[.．。]nginx(?<type>yml|csv)?$", RegexOption.IGNORE_CASE),
	weight = 10.0,
	needAdmin = true,
	canPrivate = false,
	canGroup = true
) {
	private val formater = DateTimeFormatter.ofPattern("uuuu-MM-dd HH-mm")
	private val nowFile: String get() = formater.format(LocalDateTime.now())
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message {
		if (NginxLogHandle.logSize() < 512) {
			return "日志量过少".toPlainText()
		}
		val group = PlugConfig.getAdminGroup(event.bot)
		group.launch {
			val csv = NginxLogHandle.log2csv().toByteArray().toExternalResource(".csv").toAutoCloseable()
			group.files.uploadNewFile("$nowFile.csv", csv)
			val yml = NginxLogHandle.log2Yml().toByteArray().toExternalResource(".yml").toAutoCloseable()
			group.files.uploadNewFile("$nowFile.yml", yml)
			NginxLogHandle.flushFiles()
		}
		return "正在发送".toPlainText()
	}

	private val ipRegex =
		Regex("((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])")

	@AutoCall(
		name = "添加banip",
		regex = RegexAnn("^[.．。]banip(.+)$", RegexOption.IGNORE_CASE),
		weight = 10.0,
		needAdmin = true
	)
	private fun addIP(result: MatchResult): String {
		val s = result.groups[1]?.value ?: return "未匹配到ip"
		val list = ipRegex.findAll(s).map(MatchResult::value).toMutableList()
		if (list.isEmpty()) {
			return "未匹配到ip"
		}
		return list.joinToString("\n") {
			try {
				val start = ProcessBuilder("ipset", "add", "banip", it).start()
				start.waitFor()
				it + " -> " + BufferedReader(InputStreamReader(start.errorStream)).use(BufferedReader::readLine)
			} catch (e: Exception) {
				logger.error(e)
				"执行出错：$it"
			}
		}
	}
}
