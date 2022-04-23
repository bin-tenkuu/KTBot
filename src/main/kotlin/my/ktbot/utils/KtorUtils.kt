package my.ktbot.utils

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import my.ktbot.PlugConfig
import my.ktbot.dao.*
import my.ktbot.dao.blibili.BaseApi
import my.ktbot.dao.blibili.LiveData
import my.ktbot.dao.blibili.RoomInit
import java.nio.charset.StandardCharsets

object KtorUtils {
	@JvmStatic
	val json = Json {
		encodeDefaults = false
		ignoreUnknownKeys = true
		isLenient = false
		allowStructuredMapKeys = false
		prettyPrint = false
		coerceInputValues = true
		useArrayPolymorphism = false
		allowSpecialFloatingPointValues = true
		useAlternativeNames = true
	}

	/** 使用代理的Ktor客户端 */
	@JvmStatic
	private val httpClient = HttpClient(OkHttp) {
		engine {
			if (PlugConfig.socksProxy.isNotBlank())
				proxy = ProxyBuilder.socks(PlugConfig.socksProxy, PlugConfig.socksPort)
			else if (PlugConfig.httpProxy.isNotBlank())
				proxy = ProxyBuilder.http(PlugConfig.httpProxy)
		}
		install(JsonFeature) {
			serializer = KotlinxSerializer(json)
		}
		install(HttpPlainText) {
			register(StandardCharsets.UTF_8, 1.0F)
			sendCharset = StandardCharsets.UTF_8
			responseCharsetFallback = StandardCharsets.UTF_8
		}
		install(HttpTimeout) {
			this.requestTimeoutMillis = 30_0000
			this.connectTimeoutMillis = 30_0000
			this.socketTimeoutMillis = 30_0000
		}
	}

	@JvmStatic
	fun post(urlString: String, body: Any): HttpStatement {
		return HttpStatement(HttpRequestBuilder().apply {
			method = HttpMethod.Post
			url.takeFrom(urlString)
			header("Content-Type", "application/json")
			this.body = body
		}, httpClient)
	}

	@JvmStatic
	fun get(urlString: String, block: HttpRequestBuilder.() -> Unit = {}): HttpStatement {
		return HttpStatement(HttpRequestBuilder().apply {
			method = HttpMethod.Get
			url.takeFrom(urlString)
			block()
		}, httpClient)
	}

	@JvmStatic
	suspend fun pixivCat(pid: Int): PixivCat {
		return post("https://api.pixiv.cat/v1/generate", PixivCatRequest(pid)).receive()
	}

	@JvmStatic
	suspend fun lolicon(request: LoliconRequest): LoliconResponse {
		return post("https://api.lolicon.app/setu/v2", request).receive()
	}

	/**
	 * https://github.com/SocialSisterYi/bilibili-API-collect
	 */
	@JvmStatic
	suspend fun bilibiliLive(id: Int): List<String> {
		val baseApi = get(
			"https://api.live.bilibili.com/room/v1/Room/room_init"
		) { parameter("id", id) }.receive<BaseApi<RoomInit>>()
		if (baseApi.code != 0) return listOf(baseApi.message)
		else if (baseApi.data.liveStatus != 1) return listOf("bilibili $id 未开播")
		val roomId = baseApi.data.roomId
		val durl = get(
			"https://api.live.bilibili.com/room/v1/Room/playUrl"
		) {
			parameter("cid", roomId)
			parameter("qn", 10000)
			parameter("platform", "web")
		}.receive<BaseApi<LiveData>>().data.durl
		return durl.map(LiveData.Durl::url)
	}
}
