package my.ktbot.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import my.ktbot.PlugConfig
import my.ktbot.interfaces.Plug
import java.io.*

object NginxLogHandle {

	private val nginxRegex = Regex(
		"^(?<ip>[^ ]+) - (?<user>[^ ]+) \\[(?<time>[^]]+)] \"(?:(?<method>[A-Z]{3,7}) )?(?<url>[^ ]*)(?: (?<protocol>[^\"]+))?\" (?<status>\\d+) (?<bytes>\\d+) \"(?<referer>[^\"]*)\" \"(?<agent>[^\"]*)\"(?<extra>.*)",
	)

	@Serializable
	private data class LogNginx(
		val fromFile: String,
		val ip: String,
		val user: String, // -
		val time: String, // 26/Mar/2022:08:17:25 +0000
		val status: Int, // 404
		val referer: String, // -
		val bytes: Int, // 153
		val method: String?, // GET
		val url: String, // /
		val protocol: String?, // HTTP/1.1
		val agent: String, // -
		val extra: String,
	) {

		constructor(result: MatchResult, fromFile: String) : this(
			fromFile = fromFile,
			ip = result["ip"]!!,
			user = result["user"]!!,
			time = result["time"]!!,
			status = result["status"]!!.toInt(),
			referer = result["referer"]!!,
			bytes = result["bytes"]!!.toInt(),
			method = result["method"],
			url = result["url"]!!,
			protocol = result["protocol"],
			agent = result["agent"]!!,
			extra = result["extra"]!!,
		)

		fun test(): Boolean {
			if (status / 100 == 2) {
				return false
			}
			return true
		}

		fun toCsv() = "$fromFile,$ip,$user,$time,$status,$referer,$bytes,$method,$url,$protocol,$agent,$extra"

		companion object {
			fun toCsv() = "fromFile,ip,user,time,status,referer,bytes,method,url,protocol,agent,extra"
		}
	}

	fun log2csv(): String {
		val lists = logMap
			.map(Map.Entry<*, List<LogNginx>>::value)
			.sortedByDescending(List<LogNginx>::size)
		val sequence = lists.asSequence().flatten()
		val it = StringBuilder(lists.size.shl(6))
		it.appendLine(LogNginx.toCsv())
		for (log in sequence) it.appendLine(log.toCsv())
		return it.toString()
	}

	fun log2Yml(): String {
		val map = logMap.toList().sortedByDescending { it.second.size }
		val it = StringBuilder(map.size.shl(7))
		for ((ip, list) in map) {
			it.append(ip)
			it.append(":\n")
			for (logNginx in list) {
				it.append("  - ")
				it.append(KtorUtils.json.encodeToString(serializer(), logNginx))
				it.append("\n")
			}
		}
		return it.toString()
	}

	private var files: ArrayList<File> = ArrayList()

	private val logMap: HashMap<String, MutableList<LogNginx>> = HashMap()
		get() {
			if (field.size == 0) {
				for (file in getConfFileList(PlugConfig.nginxLogPath)) {
					val name = file.name
					Plug.logger.info("Read File: $name")
					BufferedReader(FileReader(file)).use {
						for (line in it.lines()) {
							val result = nginxRegex.find(line, 0)
							if (result == null) {
								Plug.logger.info("\tNot Match Line: $line")
								continue
							}
							val element = LogNginx(result, name)
							if (element.test()) {
								field.computeIfAbsent(element.ip) { ArrayList() }.add(element)
							}
						}
					}
				}
			}
			return field
		}

	fun logSize(): Long {
		return getConfFileList(PlugConfig.nginxLogPath).sumOf(File::length)
	}

	/**
	 * 根据传入路径返回路径下全部文件，确保 <code>File.isFile() == true </code>
	 * @param path String
	 * @return List<File>
	 */
	private fun getConfFileList(path: String): List<File> {
		val pathFile = File(path)
		if (!pathFile.exists() || !pathFile.isDirectory) {
			return emptyList()
		}
		val list = (pathFile.listFiles() ?: emptyArray<File>()).filter(File::isFile)
		files.addAll(list)
		return list
	}

	fun flushFiles() {
		while (files.size > 0) {
			val file = files.removeAt(files.size - 1)
			BufferedWriter(FileWriter(file, false)).use {
				it.newLine()
			}
			logMap.clear()
		}
	}

	private operator fun MatchResult.get(index: String) = groups[index]?.value
}
