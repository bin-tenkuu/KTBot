package my.ktbot.ktor.dao

import cn.hutool.core.compress.ZipWriter
import io.ktor.server.websocket.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.serializer
import my.ktbot.ktor.vo.Message
import my.ktbot.utils.Sqlite.limit
import my.ktbot.utils.global.jsonGlobal
import org.ktorm.database.Database
import org.ktorm.dsl.deleteAll
import org.ktorm.dsl.desc
import org.ktorm.dsl.less
import org.ktorm.entity.*
import org.ktorm.support.sqlite.SQLiteDialect
import org.ktorm.support.sqlite.insertOrUpdate
import org.ktorm.support.sqlite.insertReturning
import java.io.ByteArrayInputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

/**
 * @author bin
 * @version 1.0.0
 * @Date:2023/3/12
 */
class RoomConfig(
    val id: String,
    var name: String,
    var roles: MutableMap<String, RoleConfig> = HashMap(),
) : Closeable {
    val clients = HashSet<DefaultWebSocketServerSession>()
    private val dataSource: Database = Database.connect(
        url = "jdbc:sqlite:${id}.db",
        driver = "org.sqlite.JDBC",
        dialect = SQLiteDialect(),
        alwaysQuoteIdentifiers = true,
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
                it.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS Role(
                    id TEXT PRIMARY KEY ,
                    name TEXT ,
                    tags TEXT
                    )
                """.trimIndent())
            }
        }
        dataSource.sequenceOf(TRole).forEach {
            val tags = jsonGlobal.decodeFromString<MutableList<Tag>>(it.tags)
            roles[it.id] = RoleConfig(it.id, it.name, tags)
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

    private fun save(id: Long, msg: String, role: String): Long {
        return dataSource.insertReturning(THisMsg, THisMsg.id) {
            set(it.id, id)
            set(it.msg, msg)
            set(it.role, role)
        } as Long
    }

    fun saveRoles() {
        dataSource.deleteAll(TRole)
        for ((id, config) in this.roles.entries) {
            val string = jsonGlobal.encodeToString(serializer(), config.tags)
            dataSource.insertOrUpdate(TRole) {
                set(it.id, id)
                set(it.name, config.name)
                set(it.tags, string)
                onConflict(it.id) {
                    set(it.name, excluded(it.name))
                    set(it.tags, excluded(it.tags))
                }
            }
        }
    }

    suspend fun sendAll(msg: Message) {
        for (client in clients) {
            client.sendSerialized(msg)
        }
    }

    /**
     * 关闭时清空数据，需要外部发送关闭 clients 消息
     */
    override fun close() {
        roles.clear()
        clients.clear()
    }

    fun history(id: Long): Message.Msgs {
        val list = dataSource.sequenceOf(THisMsg)
            .filter { it.id less id }
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
        val roles: Map<String, RoleConfig> = roles
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

