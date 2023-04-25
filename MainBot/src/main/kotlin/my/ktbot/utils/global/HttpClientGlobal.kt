package my.ktbot.utils.global

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import my.ktbot.PlugConfig
import my.ktbot.utils.createLogger
import java.nio.charset.StandardCharsets

/**
 * @author bin
 * @since 2023/02/17
 */

const val userAgent =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36 Edg/96.0.1054.57"

/** 使用代理的Ktor客户端 */
val httpClientGlobal = HttpClient(OkHttp) {
    engine {
        Debugger.no {
            val p = PlugConfig.proxy
            proxy = when {
                p.socks.isNotBlank() -> ProxyBuilder.socks(p.socks, p.port)
                p.http.isNotBlank() -> ProxyBuilder.http(p.http)
                else -> null
            }
        }
    }
    Debugger.yes {
        install(Logging) {
            logger = object : Logger {
                private val log = createLogger<HttpClient>()
                override fun log(message: String) {
                    log.debug(message)
                }
            }
            level = LogLevel.BODY
        }
    }
    // install(ContentEncoding) {
    // 	gzip(null)
    // 	deflate(null)
    // }
    // install(UserAgent) {
    //     agent = userAgent
    // }
    install(ContentNegotiation) {
        register(ContentType.Application.Json, KotlinxSerializationConverter(jsonGlobal))
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
