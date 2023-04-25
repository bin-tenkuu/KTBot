package my.ktbot.ktor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.websocket.*
import my.ktbot.ktor.dao.Room
import my.ktbot.ktor.dao.RoomConfig
import my.ktbot.ktor.vo.Message
import my.ktbot.ktor.vo.RoomMessage

/**
 *  @Date:2023/3/18
 *  @author bin
 *  @version 1.0.0
 */
fun Route.roomApi() {
    get("/rooms") {
        val name = call.parameters["name"] ?: ""
        call.respond(roomConfig.values.filter {
            it.room.id.contains(name)
        }.map {
            mapOf("id" to it.room.id, "name" to it.room.name)
        })
    }
    route("/room") {
        get("/logs") r@{
            val room = call.getRoom() ?: return@r
            val response = call.response
            response.header("Content-Disposition", "attachment; filename=${room.room.id}.zip")
            call.respondOutputStream(ContentType.Application.OctetStream, HttpStatusCode.OK) {
                room.historyAll(this)
            }
        }
        get r@{
            val room = call.getRoom() ?: return@r
            val message = RoomMessage(room.room.id, room.room.name, room.room.roles)
            call.respond(HttpStatusCode.OK, message)
        }
        post r@{
            val receive = call.receive<RoomMessage>()
            val room = roomConfig[receive.id]
            if (room == null) {
                val config = RoomConfig(Room {
                    id = receive.id
                    name = receive.name
                    roles = receive.roles
                })
                roomConfig[receive.id] = config
                config.insert()
            } else {
                room.room.name = receive.name
                room.room.roles = receive.roles
                room.save()
                room.sendAll(Message.Roles(room.room.roles))
            }
            call.respond(HttpStatusCode.OK, true)
        }
        get("del") r@{
            val room = call.getRoom() ?: return@r
            roomConfig -= room.room.id
            for (client in room.clients) {
                client.close(CloseReason(CloseReason.Codes.NORMAL, "room deleted"))
            }
            room.delete()
            call.respond(HttpStatusCode.OK, true)
        }
    }
}
