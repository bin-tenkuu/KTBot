package my.ktbot.plugs

import my.ktbot.PluginPerm
import my.ktbot.PluginPerm.contains
import my.ktbot.annotation.Plug
import my.ktbot.annotation.SubPlugs
import my.ktbot.dao.Lolicon
import my.ktbot.dao.LoliconRequest
import my.ktbot.database.PixivPic
import my.ktbot.database.TPixivPic
import my.ktbot.utils.KtorUtils
import my.ktbot.utils.Sqlite
import my.ktbot.utils.insertOrUpdate
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.sortedBy

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
	help = """来点色图，需获取对应权限
		|可选参数：r18，key
	""".trimMargin().toPlainText(),
	deleteMSG = 20 * 1000,
	speedLimit = 5000,
	expPrivate = -8.0,
	expGroup = -5.0,
	msgLength = 4..20,
), SubPlugs {

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

	override suspend fun invoke(event: FriendMessageEvent, result: MatchResult): Message {
		val sender = event.sender
		if (AbstractPermitteeId.ExactUser(sender.id) in PluginPerm.setu) {
			return message(result, sender)
		}
		return EmptyMessageChain
	}

	override suspend fun invoke(event: GroupMessageEvent, result: MatchResult): Message {
		if (AbstractPermitteeId.ExactGroup(event.group.id) in PluginPerm.setu
			|| AbstractPermitteeId.ExactUser(event.sender.id) in PluginPerm.setu
		) {
			return message(result, event.group)
		}
		return EmptyMessageChain
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
			val response = KtorUtils.lolicon(LoliconRequest(
				r18 = if (r18) 1 else 0,
				keyword = keyword,
				size = listOf("regular", "small")
			))
			val lolicon = response.data.firstOrNull() ?: return "找不到符合关键字的色图".toPlainText()
			runCatching { savePic(lolicon) }
			val image = KtorUtils.get(
				lolicon.urls.values.firstOrNull() ?: return "未找到图片链接".toPlainText()
			).receive<ByteArray>().toExternalResource().toAutoCloseable().uploadAsImage(contact)
			contact.sendMessage("作者：${lolicon.uid}\n原图p${lolicon.p}：${lolicon.pid}")
			return image
		} catch (e: Exception) {
			logger.error(e)
			return "<WARN>:网络请求失败".toPlainText()
		}
	}

	override val subPlugs: List<Plug> = listOf(SeTuCache, SeTuSet)

	/**
	 *
	 * @author bin
	 * @since 1.0
	 * @date 2022/1/12
	 */
	object SeTuCache : Plug(
		name = "来点[<r18>]色图",
		regex = Regex("^[来來发發给給l][张張个個幅点點份d](?<r18>r18的?)?[涩色瑟铯s][图圖t]$", RegexOption.IGNORE_CASE),
		weight = 5.1,
		deleteMSG = 20 * 1000,
		needAdmin = true,
		speedLimit = 500,
		msgLength = 4..15,
		expPrivate = -5.0,
		expGroup = -3.0,
	) {
		private fun getRandomPic(r18: Int = 0): PixivPic? {
			return Sqlite[TPixivPic].sortedBy { Sqlite.random }.firstOrNull {
				when (r18) {
					0 -> it.r18 eq false
					1 -> it.r18 eq true
					else -> Sqlite(true)
				}
			}
		}

		override suspend fun invoke(event: GroupMessageEvent, result: MatchResult): Message {
			return message(result, event.group)
		}

		override suspend fun invoke(event: FriendMessageEvent, result: MatchResult): Message {
			return message(result, event.sender)
		}

		@JvmStatic
		private suspend fun message(
			result: MatchResult,
			contact: Contact,
		): CodableMessage {
			val r18 = result["r18"] !== null
			val pic = getRandomPic(if (r18) 1 else 0) ?: return EmptyMessageChain
			val image = KtorUtils.get(pic.url) {
				headers.append("referer", "https://www.pixiv.net/")
			}.receive<ByteArray>().toExternalResource().toAutoCloseable().uploadAsImage(contact)
			contact.sendMessage("作者：${pic.uid}\n原图p${pic.p}：${pic.pid}")
			return image
		}
	}

	/**
	 *
	 * @author bin
	 * @since 1.0
	 * @date 2022/1/12
	 */
	object SeTuSet : Plug(
		name = ".色图失败列表",
		regex = Regex("^[.．。]色图失败列表$"),
		weight = 3.0,
		needAdmin = true
	) {
		override suspend fun invoke(event: MessageEvent, result: MatchResult): Message {
			return setuSet.joinToString().toPlainText()
		}
	}
}