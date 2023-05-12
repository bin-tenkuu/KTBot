package my.ktbot.ktor.dao

import io.ktor.server.websocket.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author bin
 * @version 1.0.0
 * @Date:2023/3/12
 */
class RoomConfig(
        val room: Room,
) : Closeable, CoroutineScope {
    companion object : HashMap<String, RoomConfig>() {
        init {
            for (room in databaseGlobal.sequenceOf(TRoom)) {
                this[room.id] = RoomConfig(room)
            }
        }
    }

    override val coroutineContext: CoroutineContext get() = EmptyCoroutineContext

    val id: String get() = room.id
    val name: String get() = room.name
    val roles: Map<Int, RoleConfig> get() = room.roles
    val clients = HashSet<DefaultWebSocketServerSession>()
    private val dataSource: Database = Database.connect(
            url = "jdbc:sqlite:./${id}.db",
            driver = "org.sqlite.JDBC",
            dialect = SQLiteDialect(),
            generateSqlInUpperCase = true
    )
    private val matux = Mutex()

    init {
        dataSource.useConnection { conn ->
            conn.createStatement().use {
                // language=SQLite
                it.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS HisMsg(
                    id INTEGER PRIMARY KEY AUTOINCREMENT ,
                    type TEXT,
                    role INT,
                    msg TEXT
                    )
                """.trimIndent())
            }
        }
    }

    fun save(msg: Message, role: Int) {
        if (msg is Message.Msg) {
            msg.role = role
            launch(Dispatchers.Default) {
                if (msg.id == null) {
                    matux.withLock(msg) {
                        msg.id = save(msg.msgType, msg.msg, msg.role)
                    }
                } else {
                    save(msg.id!!, msg.msg, msg.role)
                }
                sendAll(msg)
            }
        }
    }

    private fun save(type: String, msg: String, role: Int): Long {
        return dataSource.insertReturning(THisMsg.Instence, THisMsg.Instence.id) {
            set(it.type, type)
            set(it.msg, msg)
            set(it.role, role)
        } as Long
    }

    private fun save(id: Long, msg: String, role: Int) {
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
                    when (it.type) {
                        "pic" -> Message.Pic(it.msg, it.id, it.role)
                        "text" -> Message.Text(it.msg, it.id, it.role)
                        "sys" -> Message.Sys(it.id, it.msg, it.role)
                        else -> Message.Msgs()
                    }
                }
        return Message.Msgs(list)
    }

    fun historyAll(outputStream: OutputStream) {
        // TODO: 导出自定义模板
        val builder = StringBuilder()
        val roles = room.roles
        for (msg in dataSource.sequenceOf(THisMsg.Instence)) {
            val config = roles[msg.role]
            val name = config?.name ?: msg.role
            val color = config?.color ?: "black"
            builder.append("<div style=\"color: ").append(color).append("\">")
            when (msg.type) {
                "text" -> builder.append("&lt;${name}&gt;: &nbsp;").append(msg.msg)
                "pic" -> builder.append("&lt;${name}&gt;: &nbsp;").append("<img alt=\"img\" src=\"${msg.msg}\"/>")
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

