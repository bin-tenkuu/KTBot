package my.ktbot.ktor.dao

import cn.hutool.core.compress.ZipWriter
import io.ktor.server.websocket.*
import io.ktor.utils.io.core.*
import my.ktbot.ktor.vo.Message
import my.ktbot.utils.Sqlite.limit
import my.ktbot.utils.global.databaseGlobal
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.map
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.sortedBy
import org.ktorm.support.postgresql.insertReturning
import java.io.ByteArrayInputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

/**
 * @author bin
 * @version 1.0.0
 * @Date:2023/3/12
 */
class RoomConfig(
        val room: Room,
) : Closeable {
    companion object : HashMap<String, RoomConfig>() {
        init {
            for (room in databaseGlobal.sequenceOf(TRoom)) {
                this[room.id] = RoomConfig(room)
            }
        }
    }

    val id: String get() = room.id
    val name: String
        get() = room.name
    val roles: MutableMap<String, RoleConfig>
        get() = room.roles
    val clients = HashSet<DefaultWebSocketServerSession>()
    private val table = THisMsg(id)

    init {
        databaseGlobal.useConnection { conn ->
            conn.createStatement().use {
                it.executeUpdate("""create table if not exists hismsg."$id"
                    (
                        id        bigserial
                                primary key,
                        type      char(4)                                not null,
                        msg       text                                   not null,
                        role      varchar                                not null,
                        send_time timestamp(0) default CURRENT_TIMESTAMP not null
                    );""".trimIndent())
            }
        }
    }

    fun save(msg: Message, role: String) {
        msg.role = role
        when (msg) {
            is Message.Text -> {
                if (msg.id == null) {
                    msg.id = save("text", msg.msg, role)
                } else {
                    save(msg.id!!, msg.msg, role)
                }
            }
            is Message.Pic -> {
                if (msg.id == null) {
                    msg.id = save("pic", msg.msg, role)
                } else {
                    save(msg.id!!, msg.msg, role)
                }
            }
            is Message.Sys -> {
                if (msg.id == null) {
                    msg.id = save("sys", msg.msg, role)
                } else {
                    save(msg.id!!, msg.msg, role)
                }
            }
            else -> return
        }
    }

    private fun save(type: String, msg: String, role: String): Long {
        return databaseGlobal.insertReturning(table, table.id) {
            set(it.type, type)
            set(it.msg, msg)
            set(it.role, role)
        } as Long
    }

    private fun save(id: Long, msg: String, role: String) {
        databaseGlobal.update(table) {
            set(it.msg, msg)
            set(it.role, role)
            where {
                it.id eq id
            }
        }
    }

    fun insert() {
        databaseGlobal.insert(TRoom) {
            set(it.id, room.id)
            set(it.name, room.name)
            set(it.roles, room.roles)
        }
    }

    fun save() {
        room.flushChanges()
    }

    suspend fun sendAll(msg: Message) {
        for (client in clients) {
            client.sendSerialized(msg)
        }
    }

    fun delete() {
        databaseGlobal.useConnection { conn ->
            conn.createStatement().use {
                it.executeUpdate("""drop table if exists hismsg."$id";""".trimIndent())
            }
        }
        room.delete()
        close()
    }

    /**
     * 关闭时清空数据，需要外部发送关闭 clients 消息
     */
    override fun close() {
        room.roles.clear()
        clients.clear()
    }

    fun history(id: Long?): Message.Msgs {
        var sequence = databaseGlobal.sequenceOf(table)
        if (id != null) {
            sequence = sequence.filter { it.id less id }
        }
        val list = sequence
                .limit(20)
                .sortedBy { it.id.desc() }
                .map {
                    when (it.type.trim()) {
                        "pic" -> Message.Pic(it.id, it.msg, it.role)
                        "text" -> Message.Text(it.id, it.msg, it.role)
                        "sys" -> Message.Sys(it.id, it.msg, it.role)
                        else -> Message.Msgs()
                    }
                }
        return Message.Msgs(list)
    }

    fun historyAll(outputStream: OutputStream) {
        // TODO: 导出自定义模板
        val builder = StringBuilder()
        val roles: Map<String, RoleConfig> = room.roles
        for (msg in databaseGlobal.sequenceOf(table)) {
            val config = roles[msg.role]
            if (config != null) {
                builder.append("<span style='color: ${config.color}'>${config.name}</span>")
            }
            builder.append(":")
            when (msg.type) {
                "text" -> builder.append("<span>${msg.msg}</span>")
                "pic" -> builder.append("<img src=\"${msg.msg}\"/>")
                "sys" -> builder.append(msg.msg)
                else -> {}
            }
            builder.append("<br/>\n")
        }
        ZipWriter.of(outputStream, StandardCharsets.UTF_8).use {
            it.add("index.html", ByteArrayInputStream(builder.toString().toByteArray()))
        }
    }
}

