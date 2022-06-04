package my

import my.ktbot.utils.MyLogger

object MyLoggerTest {
	@JvmStatic
	fun main(vararg args: String) {
		val log = MyLogger.getLogger(MyLogger::class)
		log.trace("trace")
		log.debug("debug")
		log.info("info")
		log.warn("warn")
		log.error("error")
		val java = MyLogger::class.java
		java.name
	}
}
