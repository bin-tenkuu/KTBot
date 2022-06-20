package my.ktbot.plugs

// import my.sqlite.insertOrUpdate
import my.ktbot.annotation.*
import my.ktbot.dao.Lolicon
import my.ktbot.dao.LoliconRequest
import my.ktbot.database.PixivPic
import my.ktbot.database.TPixivPic
import my.ktbot.utils.KtorUtils
import my.ktbot.utils.Sqlite
import my.ktbot.utils.Sqlite.insertOrUpdate
import my.ktbot.utils.Sqlite.setExcluded
import my.ktbot.utils.get
import my.miraiplus.annotation.MessageHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.sortedBy

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/13
 */
object CQBotPicture {
	private val logger = MiraiLogger.Factory.create(CQBotPicture::class)

	@JvmStatic
	val setuSet = mutableSetOf<String>()

	@JvmStatic
	private fun savePic(d: Lolicon) {
		val url = d.urls.values.firstOrNull() ?: return
		Sqlite[TPixivPic].insertOrUpdate {
			set(it.pid, d.pid)
			set(it.p, d.p)
			set(it.uid, d.uid)
			set(it.r18, d.r18)
			set(it.url, url)
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

	@MessageHandle("来点[<r18>][<key>]色图")
	@HasPerm("my.ktbot.binbot:setu")
	@RegexAnn("^[来來发發给給l][张張个個幅点點份d](?<r18>r18的?)?(?<keyword>.*)?[涩色瑟铯s][图圖t]$")
	@SendAuto
	@LimitAll(1000 * 60 * 10)
	@NeedExp(-8.0, -5.0)
	suspend fun invoke(event: MessageEvent, result: MatchResult): Message {
		return message(result, event.subject)
	}

	@JvmStatic
	private suspend fun message(result: MatchResult, contact: Contact): Message {
		val r18 = result["r18"] !== null
		val keyword = result["keyword"]?.value ?: ""
		if (setuSet.contains(keyword)) {
			return "找不到符合关键字的色图".toPlainText()
		}
		try {
			logger.info("开始色图")
			val response = KtorUtils.lolicon(
				LoliconRequest(
					r18 = if (r18) 1 else 0,
					keyword = keyword,
					size = listOf("regular", "small")
				)
			)
			val lolicon = response.data.firstOrNull() ?: return "找不到符合关键字的色图".toPlainText()
			runCatching { savePic(lolicon) }
			val image = KtorUtils.get(
				lolicon.urls.values.firstOrNull() ?: return "未找到图片链接".toPlainText()
			).receive<ByteArray>().toExternalResource().toAutoCloseable().uploadAsImage(contact)
			contact.sendMessage("作者：${lolicon.uid}\n原图p${lolicon.p}：${lolicon.pid}")
			return image
		}
		catch (e: Exception) {
			logger.error(e)
			return "<WARN>:网络请求失败".toPlainText()
		}
	}

	@MessageHandle("来点[<r18>]色图")
	@HasPerm("my.ktbot.binbot:setu")
	@RegexAnn("^[来來发發给給l][张張个個幅点點份d](?<r18>r18的?)?[涩色瑟铯s][图圖t]$", RegexOption.IGNORE_CASE)
	@SendAuto(recall = 20 * 1000)
	@LimitAll(1000 * 60 * 1)
	@NeedExp(-5.0, -3.0)
	@JvmStatic
	private suspend fun setuCache(event: MessageEvent, result: MatchResult): Message {
		return messageLocal(result, event.subject)
	}

	@JvmStatic
	private fun getRandomPic(r18: Boolean): PixivPic? {
		return Sqlite[TPixivPic].sortedBy { Sqlite.random }.firstOrNull { it.r18 eq r18 }
	}

	@JvmStatic
	private suspend fun messageLocal(
		result: MatchResult,
		contact: Contact,
	): CodableMessage {
		val r18 = result["r18"] !== null
		val pic = getRandomPic(r18) ?: return EmptyMessageChain
		val image = KtorUtils.get(pic.url) {
			headers.append("referer", "https://www.pixiv.net/")
		}.receive<ByteArray>().toExternalResource().toAutoCloseable().uploadAsImage(contact)
		contact.sendMessage("作者：${pic.uid}\n原图p${pic.p}：${pic.pid}")
		return image
	}

	@MessageHandle(".色图失败列表")
	@RegexAnn("^[.．。]色图失败列表$")
	@NeedAdmin
	@SendAuto
	private val failList get() = setuSet.joinToString()

}
