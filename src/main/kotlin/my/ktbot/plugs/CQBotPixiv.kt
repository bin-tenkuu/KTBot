package my.ktbot.plugs

import my.ktbot.annotation.*
import my.ktbot.utils.KtorUtils
import my.ktbot.utils.createLogger
import my.ktbot.utils.get
import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/13
 */
object CQBotPixiv {
	private val logger = createLogger<CQBotPixiv>()

	@MiraiEventHandle("看看p站<pid>[-<p>]")
	@RegexAnn("^看{1,2}p站(?<pid>\\d+)$", RegexOption.IGNORE_CASE)
	@NeedAdmin
	@SendAuto
	@LimitAll(1000 * 10)
	@NeedExp(-8.0, -5.0)
	suspend fun invoke(event: MessageEvent, result: MatchResult): Message {
		val pid: Int = result["pid"]?.run { value.trim().toIntOrNull() } ?: return "pid获取失败".toPlainText()
		return this(pid, event.subject)
	}

	@JvmStatic
	private suspend operator fun invoke(pid: Int, contact: Contact): Message {
		logger.info("p站图片请求：$pid")
		try {
			val cat = KtorUtils.pixivCat(pid)
			if (!cat.success) {
				return "请求失败".toPlainText()
			}
			if (cat.multiple && cat.originalUrlsProxy !== null) {
				return Array(cat.originalUrlsProxy.size) {
					KtorUtils.get(cat.originalUrlsProxy[it]).body<ByteArray>()
						.toExternalResource().toAutoCloseable().uploadAsImage(contact)
				}.toMessageChain()
			}
			else if (cat.originalUrlProxy !== null) {
				return KtorUtils.get(cat.originalUrlProxy).body<ByteArray>()
					.toExternalResource().toAutoCloseable().uploadAsImage(contact)
			}
			return "pid错误".toPlainText()
		}
		catch (e: Exception) {
			logger.error(e)
			return "网络请求错误或内部错误".toPlainText()
		}
	}
}
