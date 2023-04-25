package my.ktbot.ktor.dao

import org.ktorm.entity.Entity
import org.ktorm.schema.Column
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 *  @Date:2023/3/13
 *  @author bin
 *  @version 1.0.0
 */
interface HisMsg : Entity<HisMsg> {
    val id: Long
    val type: String
    val msg: String
    val role: String

    companion object : Entity.Factory<HisMsg>()
}

open class THisMsg(
    alias: String? = "hm",
) : Table<HisMsg>(tableName = "HisMsg", alias = alias, entityClass = HisMsg::class) {
    val id: Column<Long> = long("id").bindTo { it.id }.primaryKey()

    val type: Column<String> = varchar("type").bindTo { it.type }

    val msg: Column<String> = varchar("msg").bindTo { it.msg }

    val role: Column<String> = varchar("role").bindTo { it.role }

    override fun aliased(alias: String): THisMsg = THisMsg(alias)

    companion object : THisMsg()
}
