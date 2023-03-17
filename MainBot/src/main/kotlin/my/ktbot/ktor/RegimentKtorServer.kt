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
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.serializer
import my.ktbot.ktor.dao.*
import my.ktbot.utils.global.jsonGlobal
import java.time.Duration

/**
 *  @Date:2023/3/11
 *  @author bin
 *  @version 1.0.0
 */
private var regimentServer: ApplicationEngine? = null
val roomConfig = HashMap<String?, RoomConfig>().apply {
    this["a"] = RoomConfig("a", "a").apply {
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
    route("/api") {
        get("/rooms") {
            call.respond(roomConfig.keys)
        }
        route("/room") {
            post r@{
                val roomId = call.getOrBad("roomId") ?: return@r
                val roomName = call.getOrBad("roomName") ?: return@r
                roomConfig[roomId] = RoomConfig(roomId, roomName)
                return@r
            }
            delete r@{
                val room = call.getRoom() ?: return@r
                roomConfig -= room.id
                for (client in room.clients) {
                    client.close(CloseReason(CloseReason.Codes.NORMAL, "room deleted"))
                }
                room.close()
                return@r
            }
            put r@{
                val receive = call.receive<RoomMessage>()
                val room = roomConfig.computeIfAbsent(receive.id) { RoomConfig(receive.id, receive.name) }
                room.roles.putAll(receive.roles)
                return@r
            }
            get r@{
                val room = call.getRoom() ?: return@r
                RoomMessage(room.id, room.name, room.roles).let {
                    call.respond(it)
                }
            }
        }
    }
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
                    -> {
                        room.save(msg, role)
                        room.sendAll(msg)
                    }
                    is Message.Role -> {
                        role = msg.role
                        continue
                    }
                }
            }
        } catch (_: ClosedReceiveChannelException) {
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            println("client closed")
            room.clients -= this
        }
    }
}

private suspend fun DefaultWebSocketServerSession.getRoom(): RoomConfig? {
    val roomId = call.parameters["roomId"] ?: run {
        close(CloseReason(CloseReason.Codes.NORMAL, "需要 roomId"))
        return null
    }
    val room = roomConfig[roomId] ?: run {
        close(CloseReason(CloseReason.Codes.NORMAL, "房间不存在"))
        return null
    }
    return room
}

private suspend fun ApplicationCall.getOrBad(key: String): String? {
    return parameters[key] ?: run {
        respond(HttpStatusCode.BadRequest)
        return null
    }
}

private suspend fun ApplicationCall.getRoom(): RoomConfig? {
    val roomId = getOrBad("roomId") ?: return null
    val room = roomConfig[roomId] ?: run {
        respond(HttpStatusCode.BadRequest)
        return null
    }
    return room
}
