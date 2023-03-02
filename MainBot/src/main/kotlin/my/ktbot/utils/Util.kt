@file:JvmName("Util")

package my.ktbot.utils

import my.ktbot.PlugConfig
import my.ktbot.PluginMain
import my.ktbot.annotation.*
import my.miraiplus.Caller
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.error

/**
 * 给管理员发送消息
 */
suspend fun BotEvent.sendAdmin(msg: Message) {
	try {
		PlugConfig.getAdmin(bot).sendMessage(msg)
	}
	catch (e: Exception) {
		PluginMain.logger.error({ "管理员消息发送失败" }, e)
	}
}

fun Any?.toMessage(): Message? = when (this) {
	null, is Unit,
	-> null
	is Message -> this
	is CharSequence -> if (isEmpty()) emptyMessageChain() else PlainText(this)
	is Array<*> -> iterator().toMessage()
	is Iterable<*> -> iterator().toMessage()
	is Sequence<*> -> iterator().toMessage()
	is Iterator<*> -> toMessage()
	else -> PlainText(toString())
}

fun Iterator<*>.toMessage(): MessageChain = if (!hasNext()) emptyMessageChain()
else buildMessageChain {
	this@toMessage.forEach {
		it.toMessage()?.let(::add)
	}
}

inline fun <reified T> createLogger(identity: String? = null) = MiraiLogger.Factory.create(T::class, identity)

fun Caller.toHelper(): String {
	return buildString {
		append("名称：").append(name)
		for (ann in anns) {
			when (ann) {
				is Helper -> append("\n帮助：").append(ann.help)
				is LimitAll -> append("\n速度限制：").append(ann.time).append("毫秒/次")
				is NeedAdmin -> append("\n<开发者专属>")
				is RegexAnn -> append("\n正则匹配：").append(ann.pattern).append("\n匹配规则：").run {
					ann.option.joinTo(this@buildString, "、") {
						when (it) {
							RegexOption.IGNORE_CASE -> "忽略大小写"
							RegexOption.MULTILINE -> "多行文本"
							RegexOption.DOT_MATCHES_ALL -> "跨行匹配"
							else -> ""
						}
					}
				}
				is SendAuto -> append("\n撤回延时：").append(ann.recall)
				is SendAdmin -> append("\n<发送至开发者>")
				is SendGroup -> append("\n<发送至群聊>")
			}
		}
	}
}
