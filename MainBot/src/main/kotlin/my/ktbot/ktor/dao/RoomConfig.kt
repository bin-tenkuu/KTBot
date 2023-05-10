package my.ktbot.ktor.dao

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
import java.io.File
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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
    val roles: Map<String, RoleConfig>
        get() = room.roles
    val clients = HashSet<DefaultWebSocketServerSession>()

    //    private val table = THisMsg(id)
    private val dataSource: Database = Database.connect(
            url = "jdbc:sqlite:./${id}.db",
            driver = "org.sqlite.JDBC",
            dialect = SQLiteDialect(),
            generateSqlInUpperCase = true
    )

    init {
        dataSource.useConnection { conn ->
            conn.createStatement().use {
                // language=SQLite
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
        return dataSource.insertReturning(THisMsg.Instence, THisMsg.Instence.id) {
            set(it.type, type)
            set(it.msg, msg)
            set(it.role, role)
        } as Long
    }

    private fun save(id: Long, msg: String, role: String) {
        dataSource.update(THisMsg.Instence) {
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
//        databaseGlobal.useConnection { conn ->
//            conn.createStatement().use {
//                it.executeUpdate("""drop table if exists hismsg."$id";""".trimIndent())
//            }
//        }
        room.delete()
        close()
    }

    /**
     * 关闭时清空数据，需要外部发送关闭 clients 消息
     */
    override fun close() {
        clients.clear()
    }

    fun history(id: Long?): Message.Msgs {
        var sequence = dataSource.sequenceOf(THisMsg.Instence)
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
        for (msg in dataSource.sequenceOf(THisMsg.Instence)) {
            val config = roles[msg.role]
            val name = config?.name ?: msg.role
            val color = config?.color ?: "black"
            builder.append("<div style=\">").append(color).append("\">")
            builder.append("&lt;${name}&gt;: &nbsp;")
            when (msg.type) {
                "text" -> builder.append(msg.msg)
                "pic" -> builder.append("<img alt=\"img\" src=\"${msg.msg}\"/>")
                "sys" -> builder.append("<i>").append(msg.msg).append("</i>")
                else -> {}
            }
            builder.append("</div>\n")
        }
        ZipOutputStream(outputStream, StandardCharsets.UTF_8).use {
            it.setComment("导出历史记录")
            it.setLevel(9)
            it.putNextEntry(ZipEntry("index.html"))
            it.write(builder.toString().toByteArray())
            it.closeEntry()
            it.flush()
        }
    }
}

