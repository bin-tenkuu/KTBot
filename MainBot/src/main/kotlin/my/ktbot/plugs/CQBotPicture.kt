package my.ktbot.plugs

import my.ktbot.annotation.*
import my.ktbot.dao.Lolicon
import my.ktbot.dao.LoliconRequest
import my.ktbot.database.PixivPic
import my.ktbot.database.TPixivPic
import my.ktbot.utils.KtorUtils
import my.ktbot.utils.Sqlite
import my.ktbot.utils.Sqlite.limit
import my.ktbot.utils.createLogger
import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.emptyMessageChain
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.sortedBy
import kotlin.random.Random

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/13
 */
object CQBotPicture {
	@JvmStatic
	private val logger = createLogger<CQBotPicture>()

	@JvmStatic
	val setuSet = mutableSetOf<String>()

	@MiraiEventHandle("来点色图")
	@HasPerm("my.ktbot.binbot:setu")
	@RegexAnn(
		"^[来來发發给給l][张張个個幅点點份d](?<r18>r18的?)?(?<keyword>.*)?[涩色瑟铯s][图圖t]$",
		RegexOption.IGNORE_CASE
	)
	@SendAuto(recall = 20 * 1000)
	@LimitAll(1000 * 60 * 5)
	@NeedExp(-5.0, -3.0)
	@JvmStatic
	suspend fun invoke(event: MessageEvent, groups: MatchGroupCollection): Message {
		return if (Random.nextInt(0, 100) < 20)
			message(groups, event.subject)
		else
			messageLocal(groups, event.subject)
	}

	@JvmStatic
	private suspend fun message(groups: MatchGroupCollection, contact: Contact): Message {
		val r18 = groups["r18"] !== null
		if (r18) return emptyMessageChain()
		val keyword = groups["keyword"]?.value ?: ""
		if (setuSet.contains(keyword)) {
			return "找不到符合关键字的色图".toPlainText()
		}
		try {
			logger.info("开始色图")
			val lolicon = KtorUtils.lolicon(
				LoliconRequest(
					r18 = if (r18) 1 else 0,
					keyword = keyword,
					size = listOf("regular", "small")
				)
			).firstOrNull() ?: return "找不到符合关键字的色图".toPlainText()
			runCatching { savePic(lolicon) }
			val image = KtorUtils.get(
				lolicon.urls.values.firstOrNull() ?: return "未找到图片链接".toPlainText()
			).body<ByteArray>().toExternalResource().toAutoCloseable().uploadAsImage(contact)
			contact.sendMessage("作者：${lolicon.uid}\n原图p${lolicon.p}：${lolicon.pid}")
			return image
		}
		catch (e: Exception) {
			logger.error(e)
			return "<WARN>:网络请求失败".toPlainText()
		}
	}

	@JvmStatic
	private suspend fun messageLocal(groups: MatchGroupCollection, contact: Contact): CodableMessage {
		val r18 = groups["r18"] !== null
		if (r18) return emptyMessageChain()
		val pic = getRandomPic(r18) ?: return emptyMessageChain()
		val image = KtorUtils.pixivPic(pic.url).toExternalResource().toAutoCloseable().uploadAsImage(contact)
		contact.sendMessage("作者：${pic.uid}\n原图p${pic.p}：${pic.pid}")
		return image
	}

	@JvmStatic
	private fun getRandomPic(r18: Boolean): PixivPic? {
		return Sqlite[TPixivPic].limit(1).sortedBy { Sqlite.random() }.firstOrNull { it.r18 eq r18 }
	}

	@JvmStatic
	private fun savePic(d: Lolicon) {
		val url = d.urls.values.firstOrNull() ?: return
		Sqlite.insertOrUpdate(TPixivPic) {
			set(it.pid, d.pid)
			set(it.p, d.p)
			set(it.uid, d.uid)
			set(it.r18, d.r18)
			set(it.url, url)
			set(it.author, d.author)
			set(it.title, d.title)
			onConflict(it.pid, it.p) {
				set(it.uid, excluded(it.uid))
				set(it.r18, excluded(it.r18))
				set(it.url, excluded(it.url))
				set(it.author, excluded(it.author))
				set(it.title, excluded(it.title))
			}
		}
	}

	@MiraiEventHandle("色图失败列表")
	@RegexAnn("^.色图失败列表$")
	@NeedAdmin
	@SendAuto
	@JvmStatic
	private val failList get() = setuSet.joinToString()

}
