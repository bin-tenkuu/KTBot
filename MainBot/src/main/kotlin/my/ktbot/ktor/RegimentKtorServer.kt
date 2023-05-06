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
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.serialization.serializer
import my.ktbot.PluginMain
import my.ktbot.ktor.dao.RoomConfig
import my.ktbot.ktor.mirai.ServerCommandSender
import my.ktbot.ktor.vo.Message
import my.ktbot.utils.global.jsonGlobal
import my.ktbot.utils.toMessage
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.PlainText
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 *  @Date:2023/3/11
 *  @author bin
 *  @version 1.0.0
 */
private var regimentServer: ApplicationEngine? = null

fun main() {
    val port = 8088
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
    println("Server created...($port)")
    return server
}

private fun Application.regimentKtorServer() {
    install(CORS) {
        anyHost()
        methods.addAll(HttpMethod.DefaultMethods)
        allowCredentials = true
        allowOrigins { true }
        headerPredicates += { true }
        allowNonSimpleContentTypes = true
    }
    install(Compression)
    install(Routing)
    // install(Resources)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText("500: ${cause.message}", status = HttpStatusCode.InternalServerError)
        }
    }
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(10)
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
        staticFiles("/", File("./front/dist/"), "index.html") {
            enableAutoHeadResponse()
            cacheControl {
                listOf(CacheControl.MaxAge(
                        maxAgeSeconds = Duration.ofDays(1).seconds.toInt(),
                        visibility = CacheControl.Visibility.Public
                ))
            }
            modify { file, call ->
                val response = call.response
                response.lastModified(Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()))
                response.header(HttpHeaders.ContentLength, file.length())
                response.expires(LocalDateTime.now().plusDays(1))
            }
        }
        route("/api") {
            roomApi()
        }
        wsChat()
    }
}

private fun Routing.wsChat() {
    webSocket("/ws/{roomId}") {
        val room: RoomConfig = getRoom() ?: return@webSocket
        try {
            room.clients += this
            sendSerialized(Message.Roles(room.room.roles) as Message)
            var role = ""
            while (true) {
                val msg = when (val frame = incoming.receive()) {
                    is Frame.Close -> break
                    is Frame.Ping -> {
                        send(Frame.Pong(frame.data))
                        continue
                    }
                    is Frame.Pong -> continue
                    is Frame.Binary -> continue
                    is Frame.Text -> jsonGlobal.decodeFromString(serializer<Message>(), frame.readText())
                }
                when (msg) {
                    is Message.Text -> {
                        room.save(msg, role)
                        handleBot(room, role, msg.msg)
                        room.sendAll(msg)
                    }
                    is Message.Pic -> {
                        room.save(msg, role)
                        room.sendAll(msg)
                    }
                    is Message.Default -> {
                        role = msg.role
                        val history = room.history(msg.id)
                        sendSerialized(history as Message)
                        continue
                    }
                    else -> continue
                }
            }
        } catch (_: ClosedReceiveChannelException) {
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            room.clients -= this
        }
    }
}

suspend fun DefaultWebSocketServerSession.getRoom(): RoomConfig? {
    val roomId = call.parameters["roomId"] ?: run {
        close(CloseReason(CloseReason.Codes.NORMAL, "需要 roomId"))
        return null
    }
    val room = RoomConfig[roomId] ?: run {
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

suspend fun ApplicationCall.getRoom(): RoomConfig? {
    val roomId = getOrBad("id") ?: return null
    val room = RoomConfig[roomId] ?: run {
        respond(HttpStatusCode.BadRequest)
        return null
    }
    return room
}

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
private fun handleBot(room: RoomConfig, role: String, msg: String) {
    if (true) {
        val sender = ServerCommandSender(room, role)
        sender.launch {
            CommandManager.executeCommand(sender, PlainText(msg), true)
        }
    } else {
        GlobalScope.launch {
            try {
                for (caller in PluginMain.callers) {
                    val message = caller.invoke(role, msg).toMessage()?.contentToString()
                    if (message.isNullOrBlank()) {
                        continue
                    }
                    val text = Message.Text(message)
                    room.save(text, "bot")
                    room.sendAll(text)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
