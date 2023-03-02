package my

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

/**
 * @author bin
 * @since 2023/02/15
 */
object QqChannel {
	private val client = HttpClient {
		install(WebSockets)
	}

	@JvmStatic
	fun main(args: Array<String>) {
		runBlocking {
			TestProxy(client).get("/gateway").pl()
		}
	}

	class TestProxy(
		private val client: HttpClient,
	) {
		suspend fun get(path: String): String {
			val response = client.get("https://sandbox.api.sgroup.qq.com") {
				url.appendPathSegments(path)
				header("Authorization", "Bot 102037895.h7K9eBPvEfMhN7QCGq2QCwQVX7mXllxz")
			}.body<String>()
			return response
		}
	}

}
