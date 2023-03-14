package my.ktbot.ktor.dao

import io.ktor.server.websocket.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import my.ktbot.utils.global.jsonGlobal
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.removeIf
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.update
import org.ktorm.support.sqlite.SQLiteDialect
import org.ktorm.support.sqlite.insertReturning

/**
 * @author bin
 * @version 1.0.0
 * @Date:2023/3/12
 */
@Serializable
class Tag(val key: String, val type: String = "", val color: String = "")

class RoomConfig(
    val name: String,
) {

    val roles = HashMap<String?, MutableList<Tag>>()
    val clients = HashSet<DefaultWebSocketServerSession>()
    private val dataSource: Database = Database.connect(
        url = "jdbc:sqlite:${name}.db",
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
                    name TEXT PRIMARY KEY ,
                    tags TEXT
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
            is Message.Roles -> saveRoles(msg.roles)
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

    private fun saveRoles(roles: MutableMap<String?, MutableList<Tag>>) {
        val sequence = dataSource.sequenceOf(TRole)
        val iterator = this.roles.entries.iterator()
        for (entry in iterator) {
            val (name, tags) = entry
            if (name == null) {
                continue
            }
            val list = roles.remove(name)
            if (list.isNullOrEmpty()) {
                iterator.remove()
                sequence.removeIf { it.name eq name }
                continue
            } else {
                entry.setValue(list)
                val string = jsonGlobal.encodeToString(serializer(), tags)
                sequence.update(Role(name, string))
            }
        }
        for ((name, tags) in roles.entries) {
            if (name == null) {
                continue
            }
            val string = jsonGlobal.encodeToString(serializer(), tags)
            this.roles[name] = tags
            dataSource.insert(TRole) {
                set(it.name, name)
                set(it.tags, string)
            }
        }
        roles.putAll(this.roles)
    }

    suspend fun sendAll(msg: Message) {
        for (client in clients) {
            client.sendSerialized(msg)
        }
    }
}
