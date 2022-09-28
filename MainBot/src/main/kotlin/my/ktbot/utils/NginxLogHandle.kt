package my.ktbot.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import my.ktbot.PlugConfig
import java.io.*

object NginxLogHandle {
	private val logger = createLogger<NginxLogHandle>()

	//	'$remote_addr - $remote_user [$time_iso8601] "$request"'
	//	' $status,$body_bytes_sent "$http_referer" "$http_user_agent"'
	//	',"$http_x_forwarded_for","$http_host"';
	private val nginxRegex = Regex(
		"^(?<ip>[^ ]+) - (?<user>[^ ]+) \\[(?<time>[^]]+)] \"(?:(?<method>[A-Z]{3,7}) )?(?<url>[^ ]*)(?: (?<protocol>[^\"]+))?\"" +
			" (?<status>\\d+) (?<bytes>\\d+) \"(?<referer>[^\"]*)\" \"(?<agent>[^\"]*)\"" +
			",(?<extra>.*)",
	)

	@Serializable
	private data class LogNginx(
		val fromFile: String,
		val ip: String,
		val user: String, // -
		val time: String, // 26/Mar/2022:08:17:25 +0000
		val status: String, // 404
		val referer: String, // -
		val bytes: String, // 153
		val method: String?, // GET
		val url: String, // /
		val protocol: String?, // HTTP/1.1
		val agent: String, // -
		val extra: String,
	) {

		constructor(result: MatchGroupCollection, fromFile: String) : this(
			fromFile = fromFile,
			ip = result["ip"]?.value!!,
			user = result["user"]?.value!!,
			time = result["time"]?.value!!,
			status = result["status"]?.value!!,
			referer = result["referer"]?.value!!,
			bytes = result["bytes"]?.value!!,
			method = result["method"]?.value,
			url = result["url"]?.value!!,
			protocol = result["protocol"]?.value,
			agent = result["agent"]?.value!!,
			extra = result["extra"]?.value!!,
		)

		val success get() = status.length != 3 || status[0] != '2'
		val warn get() = warnReg.containsMatchIn(url)
		fun toCsv() =
			"$fromFile,$ip,$user,$time,$status,\"$referer\",$bytes,$method,\"$url\",$protocol,\"$agent\",$extra"

		companion object {
			val warnReg = Regex("-rf|wget")
			fun toCsv() = "fromFile,ip,user,time,status,referer,bytes,method,url,protocol,agent,forwarded,host"
		}
	}

	fun log2csv(): String {
		val lists = logMap()
			.map { it.value }
			.sortedByDescending { it.size }
		val sequence = lists.asSequence().flatten()
		val it = StringBuilder(lists.size.shl(6))
		it.appendLine(LogNginx.toCsv())
		for (log in sequence) it.appendLine(log.toCsv())
		return it.toString()
	}

	fun log2Yml(): String {
		val map = logMap().entries.sortedByDescending { it.value.size }
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

	private val pass = HashSet<String>()
	private val black = HashSet<String>()
	private fun logMap(): HashMap<String, MutableList<LogNginx>> {
		if (logMap.size != 0) return logMap
		for (file in getConfFileList(PlugConfig.nginxLogPath)) {
			val name = file.name
			logger.info("Read File: $name")
			BufferedReader(FileReader(file)).use {
				for (line in it.lines()) {
					val result = nginxRegex.find(line, 0)
						?: // logger.info("\tNot Match Line: $line")
						continue
					val element = LogNginx(result.groups, name)
					if (element.method === null) continue
					val ip = element.ip
					if (ip in pass) continue
					else if (element.success) {
						pass += ip
						black -= ip
						logMap -= ip
						continue
					}
					else if (ip in black) continue
					else if (element.warn) {
						black += ip
						logMap -= ip
						continue
					}
					else logMap.computeIfAbsent(ip) { ArrayList() }.add(element)
				}
			}
		}
		banip(black)
		black.clear()
		return logMap
	}

	private val logMap: HashMap<String, MutableList<LogNginx>> = HashMap()

	fun logSize() = logMap().size

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
		val list = (pathFile.listFiles() ?: emptyArray<File>()).filter { it.isFile }
		files.addAll(list)
		return list
	}

	fun flushFiles() {
		logMap.clear()
		while (files.size > 0) {
			val file = files.removeAt(files.size - 1)
			BufferedWriter(FileWriter(file, false)).use {
				it.newLine()
			}
		}
	}

	private var banipResult = ArrayList<String>()

	fun banipResult(): ArrayList<String> {
		val list = banipResult
		banipResult = ArrayList()
		return list
	}

	fun banip(ips: Iterable<String>) {
		for (ip in ips) {
			banipResult += try {
				val start = ProcessBuilder("ipset", "add", "banip", ip).start()
				start.waitFor()
				ip + " -> " + BufferedReader(InputStreamReader(start.errorStream)).use { it.readLine() }
			}
			catch (e: Exception) {
				logger.error(e)
				"执行出错：$ip"
			}
		}
	}
}
