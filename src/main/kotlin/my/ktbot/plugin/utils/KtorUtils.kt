package my.ktbot.plugin.utils

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import my.ktbot.plugin.PlugConfig
import my.ktbot.plugin.dao.*
import my.ktbot.plugin.dao.blibili.BaseApi
import my.ktbot.plugin.dao.blibili.LiveData
import my.ktbot.plugin.dao.blibili.RoomInit

object KtorUtils {
	@JvmStatic
	val json = Json {
		ignoreUnknownKeys = true
		coerceInputValues = true
	}

	/** 使用代理的Ktor客户端 */
	@JvmStatic
	val httpClient by lazy {
		HttpClient(OkHttp) {
			engine {
				if (PlugConfig.socksProxy.isNotBlank())
					proxy = ProxyBuilder.socks(PlugConfig.socksProxy, PlugConfig.socksPort)
				else if (PlugConfig.httpProxy.isNotBlank())
					proxy = ProxyBuilder.http(PlugConfig.httpProxy)
			}
			install(JsonFeature) {
				serializer = KotlinxSerializer(json)
			}
			install(HttpTimeout) {
			}
		}
	}

	@JvmStatic
	suspend inline fun <reified T> post(urlString: String, body: Any) = httpClient.request<T>(urlString) {
		method = HttpMethod.Post
		url.takeFrom(urlString)
		header("Content-Type", "application/json")
		this.body = body
	}

	suspend fun pixivCat(pid: Int) =
		post<PixivCat>("https://api.pixiv.cat/v1/generate", PixivCatRequest(pid))

	suspend fun lolicon(request: LoliconRequest) =
		post<LoliconResponse>("https://api.lolicon.app/setu/v2", request)

	/**
	 * https://github.com/SocialSisterYi/bilibili-API-collect
	 */
	suspend fun bilibiliLive(id: Int): List<String> {
		val baseApi = httpClient.get<BaseApi<RoomInit>>(
			"https://api.live.bilibili.com/room/v1/Room/room_init"
		) { parameter("id", id) }
		if (baseApi.code != 0) return listOf(baseApi.message)
		else if (baseApi.data.liveStatus != 1) return listOf("bilibili $id 未开播")
		val roomId = baseApi.data.roomId
		val durl = httpClient.get<BaseApi<LiveData>>(
			"http://api.live.bilibili.com/room/v1/Room/playUrl"
		) {
			parameter("cid", roomId)
			parameter("qn", 10000)
			parameter("platform", "web")
		}.data.durl
		return durl.map { it.url }
	}
}
