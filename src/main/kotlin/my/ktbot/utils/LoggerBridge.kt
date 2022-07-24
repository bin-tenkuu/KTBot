package my.ktbot.utils

import net.mamoe.mirai.utils.MiraiLogger
import org.ktorm.logging.Logger

/**
 *  @since:2022/1/2
 *  @author bin
 */
class LoggerBridge(private val logger: MiraiLogger) : Logger, io.ktor.client.features.logging.Logger {
	override fun debug(msg: String, e: Throwable?) = logger.debug(msg, e)
	override fun error(msg: String, e: Throwable?) = logger.error(msg, e)
	override fun info(msg: String, e: Throwable?) = logger.info(msg, e)
	override fun isDebugEnabled(): Boolean = logger.isDebugEnabled
	override fun isErrorEnabled(): Boolean = logger.isErrorEnabled
	override fun isInfoEnabled(): Boolean = logger.isInfoEnabled
	override fun isTraceEnabled(): Boolean = logger.isVerboseEnabled
	override fun isWarnEnabled(): Boolean = logger.isWarningEnabled
	override fun trace(msg: String, e: Throwable?) = logger.verbose(msg, e)
	override fun warn(msg: String, e: Throwable?) = logger.warning(msg, e)
	override fun log(message: String) = logger.info(message)
}
