package my.ktbot.ktor.dao

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import my.ktbot.utils.global.jsonGlobal
import org.ktorm.entity.Entity
import org.ktorm.schema.Column
import org.ktorm.schema.SqlType
import org.ktorm.schema.Table
import org.ktorm.schema.varchar
import org.postgresql.util.PGobject
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

/**
 * @author bin
 * @since 2023/04/25
 */
interface Room : Entity<Room> {
    var id: String
    var name: String
    var roles: Map<Int, RoleConfig>

    companion object : Entity.Factory<Room>()
}

@Serializable
class RoomMessage(
        val id: String,
        val name: String,
        val roles: Map<Int, RoleConfig>,
) {
    constructor(room: Room) : this(room.id, room.name, room.roles)

    fun toRoom() = Room {
        id = this.id
        name = this.name
        roles = this.roles
    }
}

object TRoom : Table<Room>(tableName = "room", entityClass = Room::class) {
    val id: Column<String> = varchar("id").bindTo { it.id }.primaryKey()
    val name: Column<String> = varchar("name").bindTo { it.name }
    val roles = registerColumn("roles", RoleSqlType).bindTo { it.roles }
}

object RoleSqlType : SqlType<Map<Int, RoleConfig>>(Types.VARCHAR, "varchar") {
    private val serializer = serializer<Map<Int, RoleConfig>>()

    override fun doSetParameter(ps: PreparedStatement, index: Int, parameter: Map<Int, RoleConfig>) {
        val s = jsonGlobal.encodeToString(serializer, parameter)
        ps.setObject(index, PGobject().apply {
            type = "json"
            value = s
        }, Types.OTHER)
    }

    override fun doGetResult(rs: ResultSet, index: Int): Map<Int, RoleConfig> {
        val s = rs.getString(index)
        return jsonGlobal.decodeFromString(serializer, s)
    }
}

@Serializable
class RoleConfig(
        val id: Int,
        val name: String,
        val color: String = "",
)
