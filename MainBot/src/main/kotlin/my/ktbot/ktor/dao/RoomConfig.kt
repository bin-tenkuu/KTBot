package my.ktbot.ktor.dao

import cn.hutool.core.compress.ZipWriter
import io.ktor.server.websocket.*
import io.ktor.utils.io.core.*
import my.ktbot.ktor.vo.Message
import my.ktbot.utils.Sqlite.limit
import my.ktbot.utils.global.databaseGlobal
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.map
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.sortedBy
import org.ktorm.support.sqlite.SQLiteDialect
import org.ktorm.support.sqlite.insertReturning
import java.io.ByteArrayInputStream
import java.io.File
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
    var name: String
        get() = room.name
        set(value) {
            room.name = value
        }
    var roles: MutableMap<String, RoleConfig>
        get() = room.roles
        set(value) {
            room.roles = value
        }
    val clients = HashSet<DefaultWebSocketServerSession>()
    private val dataSource: Database = Database.connect(
            url = "jdbc:sqlite:./${id}.db",
            driver = "org.sqlite.JDBC",
            dialect = SQLiteDialect(),
            generateSqlInUpperCase = true
    )

    init {
        dataSource.useConnection { conn ->
            conn.createStatement().use {
                it.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS HisMsg(
                    id INTEGER PRIMARY KEY AUTOINCREMENT ,
                    type TEXT,
                    msg TEXT,
                    role TEXT
                    )
                """.trimIndent())
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

            else -> return
        }
    }

    private fun save(type: String, msg: String, role: String): Long {
        return dataSource.insertReturning(THisMsg, THisMsg.id) {
            set(it.type, type)
            set(it.msg, msg)
            set(it.role, role)
        } as Long
    }

    private fun save(id: Long, msg: String, role: String) {
        dataSource.update(THisMsg) {
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
        File("${room.id}.db").delete()
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
        var sequence = dataSource.sequenceOf(THisMsg)
        if (id != null) {
            sequence = sequence.filter { it.id less id }
        }
        val list = sequence
                .limit(20)
                .sortedBy { it.id.desc() }
                .map {
                    when (it.type) {
                        "pic" -> Message.Pic(it.id, it.msg, it.role)
                        "text" -> Message.Text(it.id, it.msg, it.role)
                        else -> Message.Msgs()
                    }
                }
        return Message.Msgs(list)
    }

    fun historyAll(outputStream: OutputStream) {
        val builder = StringBuilder()
        val roles: Map<String, RoleConfig> = room.roles
        for (msg in dataSource.sequenceOf(THisMsg)) {
            val config = roles[msg.role]
            if (config != null) {
                for (tag in config.tags) {
                    builder.append("<span>${tag.name}</span>")
                }
            } else {
                builder.append("<span>${msg.role}</span>")
            }
            builder.append(":")
            when (msg.type) {
                "text" -> builder.append("<span>${msg.msg}</span>")
                "pic" -> builder.append("<img src=\"${msg.msg}\"/>")
                else -> {}
            }
            builder.append("<br/>\n")
        }
        ZipWriter.of(outputStream, StandardCharsets.UTF_8).use {
            it.add("index.html", ByteArrayInputStream(builder.toString().toByteArray()))
        }
    }
}

