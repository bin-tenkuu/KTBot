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
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.serializer
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
    this["a"] = RoomConfig("a").apply {
        roles["a"] = mutableListOf(Tag("a"))
        roles["b"] = mutableListOf(
            Tag("b"),
            Tag("success", "success"),
            Tag("info", "info"),
            Tag("warning", "warning"),
            Tag("danger", "danger"),
        )
    }
}

fun main() {
    val port = 80
    println("Starting server...($port)")
    server(port).start(true)
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
        try {
            room.clients += this
            sendSerialized(Message.Roles(room.roles) as Message)
            var role = ""
            while (true) {
                val msg = when (val frame = incoming.receive()) {
                    is Frame.Close -> break
                    is Frame.Ping -> {
                        send(Frame.Pong(frame.data))
                        continue
                    }
                    is Frame.Pong -> continue
                    is Frame.Binary -> break
                    is Frame.Text -> jsonGlobal.decodeFromString<Message>(serializer(), frame.readText())
                }
                when (msg) {
                    is Message.Text,
                    is Message.Pic,
                    is Message.Roles,
                    -> room.save(msg, role)
                    is Message.Role -> role = msg.role
                }
                room.sendAll(msg)
            }
        } catch (_: ClosedReceiveChannelException) {
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            room.clients -= this
        }
    }
}

private suspend fun DefaultWebSocketServerSession.getRoom(): RoomConfig? {
    val roomId = call.parameters["roomId"] ?: run {
        close(CloseReason(CloseReason.Codes.NORMAL, "需要 roomId"))
        return null
    }
    val room = roomConfig.getOrPut(roomId) {
        RoomConfig(roomId)
    }
    return room
}
