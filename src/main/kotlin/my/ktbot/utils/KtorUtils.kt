package my.ktbot.utils

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
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
	const val UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36 Edg/96.0.1054.57"

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
		// install(ContentNegotiation) {
		// 	json(json)
		// }
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
		install(Logging) {
			logger = LoggerBridge(MyLogger.create(KtorUtils::class))
			level = LogLevel.INFO
		}
	}

	@JvmStatic
	fun post(urlString: String, body: Any): HttpStatement {
		return HttpStatement(HttpRequestBuilder().apply {
			method = HttpMethod.Post
			url.takeFrom(urlString)
			header("Content-Type", "application/json")
			setBody(body)
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
		return post("https://api.pixiv.cat/v1/generate", PixivCatRequest(pid)).body()
	}

	@JvmStatic
	suspend fun lolicon(request: LoliconRequest): LoliconResponse {
		return post("https://api.lolicon.app/setu/v2", request).body()
	}

	/**
	 * https://github.com/SocialSisterYi/bilibili-API-collect
	 */
	@JvmStatic
	suspend fun bilibiliLive(id: Int): List<String> {
		val baseApi = get("https://api.live.bilibili.com/room/v1/Room/room_init") {
			parameter("id", id)
		}.body<BaseApi<RoomInit>>()
		if (baseApi.code != 0) return listOf(baseApi.message)
		else if (baseApi.data.liveStatus != 1) return listOf("bilibili $id 未开播")
		val roomId = baseApi.data.roomId
		val durl = get("https://api.live.bilibili.com/room/v1/Room/playUrl") {
			parameter("cid", roomId)
			parameter("qn", 10000)
			parameter("platform", "web")
		}.body<BaseApi<LiveData>>().data.durl
		return durl.map(LiveData.Durl::url)
	}

	/**
	 * 祖安
	 * @param max [Boolean] 火力全开
	 * @return [String]
	 */
	@Deprecated("接口无法调用")
	suspend fun zuan(max: Boolean = false): String {
		val url = if (max) "https://zuanbot.com/api.php?lang=zh_cn"
		else "https://zuanbot.com/api.php?lang=zh_cn&level=min"
		return get(url) {
			header("Accept", "*/*")
			header("Referer", "https://zuanbot.com/")
			header("User-Agent", UserAgent)
		}.body()
	}

	suspend fun rainbowFart(): String {
		val regex = Regex("(?<=\"text\":\")[^\"]+")
		val receive = get("https://api.shadiao.pro/chp") {
			header("origin", "https://chp.shadiao.app")
			header("referer", "https://chp.shadiao.app/")
			header("User-Agent", UserAgent)
		}.body<String>()
		val text = regex.find(receive)?.value ?: ""
		val string = String(text.split("\\u").mapNotNull {
			if (it.isNotBlank()) it.toInt(16).toChar() else null
		}.toCharArray())
		return string
	}

	private fun <T : Any> HttpRequestBuilder.setBody(body: T) {
		this.body = body
	}

	suspend inline fun <reified T> HttpStatement.body(): T {
		return receive()
	}
}
