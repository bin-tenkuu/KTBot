package my.ktbot.plugin.plugs

import io.ktor.client.request.*
import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.dao.Lolicon
import my.ktbot.plugin.dao.LoliconRequest
import my.ktbot.plugin.database.TPixivPic
import my.ktbot.plugin.utils.KtorUtils
import my.ktbot.plugin.utils.Sqlite
import my.ktbot.plugin.utils.insertOrUpdate
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/13
 */
object CQBotPicture : Plug(
	name = "来点[<r18>][<key>]色图",
	regex = Regex("^[来來发發给給l][张張个個幅点點份d](?<r18>r18的?)?(?<keyword>.*)?[涩色瑟铯s][图圖t]$"),
	weight = 5.0,
	help = "来点色图，可选参数：r18，key".toPlainText(),
	deleteMSG = 20 * 1000,
	needAdmin = true,
	speedLimit = 5000,
	expPrivate = -8.0,
	expGroup = -5.0,
	msgLength = 4..20
) {
	@JvmStatic
	val setuSet = mutableSetOf<String>()

	@JvmStatic
	private fun savePic(d: Lolicon) {
		Sqlite[TPixivPic].insertOrUpdate {
			set(it.pid, d.pid)
			set(it.p, d.p)
			set(it.uid, d.uid)
			set(it.r18, d.r18)
			set(it.url, d.urls.values.firstOrNull() ?: "")
			set(it.author, d.author)
			set(it.title, d.title)
			onConflict(it.pid, it.p) {
				setExcluded(it.uid)
				setExcluded(it.r18)
				setExcluded(it.url)
				setExcluded(it.author)
				setExcluded(it.title)
			}
		}
	}

	override suspend fun invoke(event: FriendMessageEvent, result: MatchResult): Message {
		return message(result, event.sender)
	}

	override suspend fun invoke(event: GroupMessageEvent, result: MatchResult): Message {
		return message(result, event.group)
	}

	@JvmStatic
	private suspend fun message(result: MatchResult, contact: Contact): Message {
		val r18 = result["r18"] !== null
		val keyword = result["keyword"]?.value ?: ""
		if (setuSet.contains(keyword)) {
			return "没有，爬".toPlainText()
		}
		try {
			logger.info("开始色图")
			val response = KtorUtils.lolicon(LoliconRequest(
				r18 = if (r18) 1 else 0,
				keyword = keyword,
				size = listOf("regular", "small")
			))
			val lolicon = response.data.firstOrNull() ?: return "找不到符合关键字的色图".toPlainText()
			runCatching{ savePic(lolicon) }
			val image = KtorUtils.httpClient.get<ByteArray>(
				lolicon.urls.values.firstOrNull() ?: return "未找到图片链接".toPlainText()
			).toExternalResource().toAutoCloseable().uploadAsImage(contact)
			contact.sendMessage("作者：${lolicon.uid}\n原图p${lolicon.p}：${lolicon.pid}")
			return image
		} catch (e: Exception) {
			logger.error(e)
			return "网络请求失败".toPlainText()
		}
	}
}