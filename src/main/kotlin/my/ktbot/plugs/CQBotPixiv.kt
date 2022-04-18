package my.ktbot.plugs

import my.ktbot.annotation.Plug
import my.ktbot.utils.KtorUtils
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
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
object CQBotPixiv : Plug(
	name = "看看p站<pid>[-<p>]",
	regex = Regex("^看{1,2}p站(?<pid>\\d+)$"),
	weight = 5.0,
	help = "看看p站带上pid发送，可选参数：p".toPlainText(),
	deleteMSG = 100 * 1000,
	speedLimit = 2000,
	expPrivate = -8.0,
	expGroup = -5.0,
	msgLength = 5..20,
) {
	override suspend fun invoke(event: GroupMessageEvent, result: MatchResult): Message {
		val pid: Int = result["pid"]?.run { value.trim().toIntOrNull() } ?: return "pid获取失败".toPlainText()
		return this(pid, event.group)
	}

	override suspend fun invoke(event: FriendMessageEvent, result: MatchResult): Message {
		val pid: Int = result["pid"]?.run { value.trim().toIntOrNull() } ?: return "pid获取失败".toPlainText()
		return this(pid, event.sender)
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
					KtorUtils.get(cat.originalUrlsProxy[it]).receive<ByteArray>()
						.toExternalResource().toAutoCloseable().uploadAsImage(contact)
				}.toMessageChain()
			}
			else if (cat.originalUrlProxy !== null) {
				return KtorUtils.get(cat.originalUrlProxy).receive<ByteArray>()
					.toExternalResource().toAutoCloseable().uploadAsImage(contact)
			}
			return "pid错误".toPlainText()
		} catch (e: Exception) {
			logger.error(e)
			return "网络请求错误或内部错误".toPlainText()
		}
	}
}