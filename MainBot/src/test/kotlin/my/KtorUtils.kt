package my

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import kotlinx.serialization.json.Json
import my.ktbot.utils.createLogger
import java.nio.charset.StandardCharsets

object KtorUtils {
	private const val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36 Edg/96.0.1054.57"
	private val logger = createLogger<KtorUtils>()

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
		engine { }
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

	@JvmStatic
	fun post(urlString: String, body: Any): HttpStatement {
		return HttpStatement(HttpRequestBuilder().apply {
			method = HttpMethod.Post
			url.takeFrom(urlString)
			header(HttpHeaders.ContentType, ContentType.Application.Json)
			setBody(body)
		}, httpClient)
	}

	@JvmStatic
	fun get(urlString: String): HttpStatement {
		return HttpStatement(HttpRequestBuilder().apply {
			method = HttpMethod.Get
			url.takeFrom(urlString)
		}, httpClient)
	}

	@JvmStatic
	fun get(urlString: String, block: HttpRequestBuilder.() -> Unit): HttpStatement {
		return HttpStatement(HttpRequestBuilder().apply {
			method = HttpMethod.Get
			url.takeFrom(urlString)
			block()
		}, httpClient)
	}
}
