package my.ktbot.plugin.utils

import net.mamoe.mirai.utils.LoggerAdapters.asMiraiLogger
import net.mamoe.mirai.utils.MiraiLogger
import java.util.logging.*
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/28
 */
class MyLogger : MiraiLogger.Factory {
	override fun create(requester: Class<*>, identity: String?): MiraiLogger {
		return logger
	}

	companion object {
		@JvmStatic
		private val path = Path("./log/log-%g.log")

		init {
			path.parent.toFile().mkdir()
		}

		@JvmStatic
		private val fileHandler = FileHandler(path.absolutePathString(), 1024L * 1024 * 1024, 5, true).apply {
			level = Level.WARNING
			formatter = SimpleFormatter()
		}

//	private val formatter = object : Formatter() {
//		private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//
//		//定义一个匿名类
//		override fun format(record: LogRecord): String {
//			return "${formatter.format(Date())} ${record.level}/${record.loggerName}: ${record.message}\n"
//		}
//	}

		@JvmStatic
		val logger = Logger.getGlobal().apply {
			level = Level.INFO
			addHandler(fileHandler)
		}.asMiraiLogger()

	}
}