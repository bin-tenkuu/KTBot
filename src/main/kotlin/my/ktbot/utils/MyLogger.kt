package my.ktbot.utils

import net.mamoe.mirai.utils.LoggerAdapters.asMiraiLogger
import net.mamoe.mirai.utils.MiraiLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/28
 */
class MyLogger : MiraiLogger.Factory by MyLogger {
	companion object : MiraiLogger.Factory {
		fun getLogger(requester: Class<*>, identity: String? = null): Logger {
			return LoggerFactory.getLogger(identity ?: requester.simpleName ?: requester.name)
		}

		fun getLogger(requester: KClass<*>, identity: String? = null): Logger = getLogger(requester.java, identity)

		override fun create(requester: Class<*>, identity: String?): MiraiLogger {
			// createLogger(requester)
			// return DirectoryLogger(identity ?: requester.simpleName ?: requester.name, File("./logs"),
			// 	Duration.ofDays(10).toMillis())
			return getLogger(requester, identity).asMiraiLogger()
		}
	}
}
