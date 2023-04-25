package my.ktbot.ktor.dao

import org.ktorm.entity.Entity
import org.ktorm.schema.Column
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

@Deprecated("use Role instead")
interface Role : Entity<Role> {
    var id: String
    var name: String
    var tags: String

    companion object : Entity.Factory<Role>()
}
@Deprecated("use Role instead")
open class TRole(
    alias: String? = "r",
) : Table<Role>(tableName = "Role", alias = alias, entityClass = Role::class) {
    val id: Column<String> = varchar("id").bindTo { it.id }.primaryKey()

    val name: Column<String> = varchar("name").bindTo { it.name }

    val tags: Column<String> = varchar("tags").bindTo { it.tags }

    override fun aliased(alias: String): TRole = TRole(alias)

    companion object : TRole()
}
