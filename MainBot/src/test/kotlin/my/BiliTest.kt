package my

import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import my.ktbot.dao.blibili.BaseApi
import my.ktbot.dao.blibili.VedioInfo
import my.ktbot.dao.blibili.VedioStreamUrl

/**
 *  @Date:2023/1/29
 *  @author bin
 *  @version 1.0.0
 */
object BiliTest {
	private const val baseUrl = "https://api.bilibili.com/x/web-interface/view"
	private const val bv = "BV1iy4y1d78B"
	private const val playUrl = "https://api.bilibili.com/x/player/playurl"

	@JvmStatic
	fun main(args: Array<String>) {
		runBlocking {
			val info = KtorUtils.get(baseUrl) {
				parameter("bvid", bv)
			}.body<String>() ?: return@runBlocking
			info.pl()
			return@runBlocking
			// "".pl()
			// val vedioUrl = KtorUtils.get(playUrl) {
			// 	parameter("bvid", bv)
			// 	parameter("cid", info.cid)
			// 	// parameter("qn", 16)
			// 	// parameter("fnval", 1)
			// 	// parameter("session", "")
			// }.body<BaseApi<VedioStreamUrl>>().data
			// vedioUrl.pl()
			// vedioUrl ?: return@runBlocking
			// "".pl()
			// if (vedioUrl.durl.isEmpty()) {
			// 	return@runBlocking
			// }
			// val durl = vedioUrl.durl[0]
			// durl.pl()
		}
		// KtorUtils.get("${playUrl}bvid=BV1iy4y1d78B")
	}
}
