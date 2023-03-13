package my.ktbot.ktor

import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.dataconversion.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import my.ktbot.ktor.dao.Message
import my.ktbot.ktor.dao.RoomConfig
import my.ktbot.ktor.dao.Tag
import my.ktbot.utils.global.jsonGlobal
import java.time.Duration

/**
 *  @Date:2023/3/11
 *  @author bin
 *  @version 1.0.0
 */
private var regimentServer: ApplicationEngine? = null
val roomConfig = HashMap<String?, RoomConfig>().apply {
    this["test"] = RoomConfig("测试房间").apply {
        roles["a"] = mutableListOf(Tag("a", ""))
        roles["b"] = mutableListOf(Tag("b", ""))
    }
}

fun main() {
    server(8080).start(true)
}

fun server(port: Int = 80): ApplicationEngine {
    if (regimentServer != null) {
        return regimentServer!!
    }
    val server = embeddedServer(
        factory = Netty,
        port = port,
        host = "0.0.0.0",
        module = {
            regimentKtorServer()
        }
    )
    regimentServer = server
    return server
}

private fun Application.regimentKtorServer() {
    install(CORS) {
        anyHost()
    }
    install(Compression)
    install(Routing)
    install(Resources)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText("500: ${cause.message}", status = HttpStatusCode.InternalServerError)
        }
    }
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(60)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(jsonGlobal)
    }
    install(ContentNegotiation) {
        register(ContentType.Application.Json, KotlinxSerializationConverter(jsonGlobal))
    }
    install(DataConversion)
    routing {
        route("/api") {
            route("room") {
                get("list") {
                    call.respond(roomConfig.keys)
                }
            }
        }
        static("/static") {
            defaultResource("regiment/index.html")
            resources("regiment")
        }
        wsChat()
    }
}

private fun Routing.wsChat() {
    webSocket("/ws/{roomId}") {
        val room: RoomConfig = getRoom() ?: return@webSocket
        println("${room.name} 新的连接")
        try {
            val role: String = getRole() ?: return@webSocket
            room.clients += this
            room.sendAll(Message.SysText("角色 $role 进入房间"))
            sendSerialized(Message.Roles(room.roles))
            while (true) {
                val msg = receiveDeserialized<Message>()
                when (msg) {
                    is Message.Roles -> {
                        // room.roles = msg.roles
                    }
                    else -> {}
                }
                room.sendAll(msg)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            println("${room.name} 连接断开")
            room.clients -= this
        }
    }
}

private suspend fun DefaultWebSocketServerSession.getRoom(): RoomConfig? {
    val roomId = call.parameters["roomId"] ?: run {
        close(CloseReason(CloseReason.Codes.NORMAL, "需要 roomId"))
        return null
    }
    return roomConfig[roomId] ?: run {
        close(CloseReason(CloseReason.Codes.NORMAL, "roomId 不存在"))
        return null
    }
}

private suspend fun DefaultWebSocketServerSession.getRole(): String? {
    val text = (incoming.receive() as? Frame.Text)?.readText()
    if (text.isNullOrEmpty()) {
        close(CloseReason(CloseReason.Codes.NORMAL, "需要定义角色"))
        return null
    }
    return text
}
