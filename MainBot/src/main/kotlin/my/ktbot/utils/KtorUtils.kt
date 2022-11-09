package my.ktbot.utils

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import my.ktbot.PlugConfig
import my.ktbot.dao.*
import my.ktbot.dao.blibili.BaseApi
import my.ktbot.dao.blibili.LiveData
import my.ktbot.dao.blibili.RoomInit
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.nio.charset.StandardCharsets

object KtorUtils {
	private const val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36 Edg/96.0.1054.57"
	private val logger = createLogger<KtorUtils>()

	/** 使用代理的Ktor客户端 */
	@JvmStatic
	private val httpClient = HttpClient(OkHttp) {
		engine {
			if (PlugConfig.socksProxy.isNotBlank())
				proxy = ProxyBuilder.socks(PlugConfig.socksProxy, PlugConfig.socksPort)
			else if (PlugConfig.httpProxy.isNotBlank())
				proxy = ProxyBuilder.http(PlugConfig.httpProxy)
		}
		install(Logging) {
			logger = object : Logger {
				override fun log(message: String) {
					KtorUtils.logger.debug(message)
				}
			}
			level = LogLevel.INFO
		}
		install(ContentNegotiation) {
			register(ContentType.Application.Json, KotlinxSerializationConverter(json))
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
		install(UserAgent) {
			agent = userAgent
		}
	}

	private fun HttpRequestBuilder.toStatement() = HttpStatement(this, httpClient)

	@JvmStatic
	fun post(urlString: String, body: Any): HttpStatement {
		return HttpRequestBuilder().apply {
			method = HttpMethod.Post
			url.takeFrom(urlString)
			header(HttpHeaders.ContentType, ContentType.Application.Json)
			setBody(body)
		}.toStatement()
	}

	@JvmStatic
	fun get(urlString: String): HttpStatement {
		return HttpRequestBuilder().apply {
			method = HttpMethod.Get
			url.takeFrom(urlString)
		}.toStatement()
	}

	@JvmStatic
	private inline fun get(urlString: String, block: HttpRequestBuilder.() -> Unit): HttpStatement {
		return HttpRequestBuilder().apply {
			method = HttpMethod.Get
			url.takeFrom(urlString)
			block()
		}.toStatement()
	}

	// region 具体请求

	@JvmStatic
	suspend fun pixivPic(url: String): ByteArray {
		return get(url) {
			header(HttpHeaders.Referrer, "https://www.pixiv.net/")
		}.body()
	}

	@JvmStatic
	suspend fun pixivCat(pid: Int): PixivCat {
		return post("https://api.pixiv.cat/v1/generate", PixivCatRequest(pid)).body()
	}

	/**
	 * lolicon
	 * todo:https://lolisuki.cc/#/setu
	 * @param request LoliconRequest
	 * @return List<Lolicon>
	 */
	@JvmStatic
	suspend fun lolicon(request: LoliconRequest): List<Lolicon> {
		return post("https://api.lolicon.app/setu/v2", request).body<LoliconResponse>().data
	}

	/**
	 * https://github.com/SocialSisterYi/bilibili-API-collect
	 */
	@Suppress("unused")
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
		return durl.map { it.url }
	}

	/**
	 * 祖安
	 * @param max [Boolean] 火力全开
	 * @return [String]
	 */
	@JvmStatic
	@Deprecated("接口无法调用")
	suspend fun zuan(max: Boolean = false): String {
		val url = if (max) "https://zuanbot.com/api.php?lang=zh_cn"
		else "https://zuanbot.com/api.php?lang=zh_cn&level=min"
		return get(url) {
			header(HttpHeaders.Accept, "*/*")
			header(HttpHeaders.Referrer, "https://zuanbot.com/")
		}.body()
	}

	/**
	 * 彩虹屁
	 * @return String
	 */
	@JvmStatic
	suspend fun rainbowFart(): String {
		val text = get("https://api.shadiao.pro/chp") {
			header(HttpHeaders.Origin, "https://chp.shadiao.app")
			header(HttpHeaders.Referrer, "https://chp.shadiao.app/")
		}.body<RainbowFart>().data.text
		println(text)
		return text
	}

	/**
	 * 绿茶/渣男语录（海王）
	 * @param tea Boolean true-F-绿茶,false-M-渣男
	 * @return String
	 */
	@JvmStatic
	suspend fun greenTea(tea: Boolean = false): String {
		return get("https://api.lovelive.tools/api/SweetNothings?genderType=" + if (tea) "F" else "M").body()
	}

	/**
	 * 60s读懂世界
	 * @return ExternalResource
	 */
	suspend fun read60s(): ExternalResource {
		return get("https://api.qqsuu.cn/api/dm-60s").body<ByteArray>().toExternalResource("png").toAutoCloseable()
	}

	suspend fun read60sJson(): EveryDay60s {
		return get("https://api.qqsuu.cn/api/dm-60s?type=json").body()
	}

	/**
	 * 能不能好好说话？
	 * @param text String
	 * @return List<String>
	 */
	suspend fun nbnhhsh(text: String): List<String> {
		val list = post("https://lab.magiconch.com/api/nbnhhsh/guess", mapOf("text" to text)).body<List<Nbnhhsh>>()
		return list.firstOrNull { it.name == text }?.trans ?: emptyList()
	}
	// endregion
}
