package my.ktbot.utils

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import kotlinx.serialization.json.Json
import my.ktbot.PlugConfig
import java.nio.charset.StandardCharsets

/**
 * @author bin
 * @since 2023/02/17
 */
private object GlobalInstance

private val log = createLogger<GlobalInstance>()
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
const val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36 Edg/96.0.1054.57"

/** 使用代理的Ktor客户端 */
val httpClient = HttpClient(OkHttp) {
	engine {
		proxy = when {
			PlugConfig.socksProxy.isNotBlank() -> ProxyBuilder.socks(PlugConfig.socksProxy, PlugConfig.socksPort)
			PlugConfig.httpProxy.isNotBlank() -> ProxyBuilder.http(PlugConfig.httpProxy)
			else -> null
		}
	}
	if (PlugConfig.debug) {
		install(Logging) {
			logger = object : Logger {
				override fun log(message: String) {
					log.debug(message)
				}
			}
			level = LogLevel.BODY
		}
	}
	install(ContentEncoding) {
		gzip(null)
		deflate(null)
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
		this.requestTimeoutMillis = 30_000
		this.connectTimeoutMillis = 30_000
		this.socketTimeoutMillis = 30_000
	}
}
