package my.ktbot.utils

import net.mamoe.mirai.utils.DirectoryLogger
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.time.Duration

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/28
 */
class MyLogger : MiraiLogger.Factory {
	override fun create(requester: Class<*>, identity: String?): MiraiLogger {
		return MyLogger.create(requester, identity)
	}

	companion object : MiraiLogger.Factory {
		override fun create(requester: Class<*>, identity: String?): MiraiLogger {
			return DirectoryLogger(identity ?: requester.simpleName ?: requester.name, File("./logs"),
				Duration.ofDays(10).toMillis())
			// return LoggerFactory.getLogger(identity ?: requester.simpleName ?: requester.name).asMiraiLogger()
		}
	}

}