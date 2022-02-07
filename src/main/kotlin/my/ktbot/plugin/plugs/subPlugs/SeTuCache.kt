package my.ktbot.plugin.plugs.subPlugs

import io.ktor.client.request.*
import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.database.PixivPic
import my.ktbot.plugin.database.TPixivPic
import my.ktbot.plugin.utils.KtorUtils
import my.ktbot.plugin.utils.Sqlite
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.sortedBy

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
	expPrivate = -5.0,
	expGroup = -3.0,
	msgLength = 4..15
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
		val image = KtorUtils.httpClient.get<ByteArray>(pic.url) {
			headers.append("referer", "https://www.pixiv.net/")
		}.toExternalResource().toAutoCloseable().uploadAsImage(contact)
		contact.sendMessage("作者：${pic.uid}\n原图p${pic.p}：${pic.pid}")
		return image
	}
}