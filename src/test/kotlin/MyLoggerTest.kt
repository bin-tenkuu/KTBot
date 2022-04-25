package org.example.mirai.plugin

import my.ktbot.utils.MyLogger

fun main() {
	val log = MyLogger.getLogger(MyLogger::class)
	log.trace("trace")
	log.debug("debug")
	log.info("info")
	log.warn("warn")
	log.error("error")
	val java = MyLogger::class.java
	java.name
}
