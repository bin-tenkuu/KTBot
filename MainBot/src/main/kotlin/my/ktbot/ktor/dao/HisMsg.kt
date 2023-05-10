package my.ktbot.ktor.dao

import org.ktorm.entity.Entity
import org.ktorm.schema.*

/**
 *  @Date:2023/3/13
 *  @author bin
 *  @version 1.0.0
 */
interface HisMsg : Entity<HisMsg> {
    val id: Long
    val type: String
    val msg: String
    val role: Int

    companion object : Entity.Factory<HisMsg>()
}

class THisMsg(tableName: String)
    : Table<HisMsg>(tableName = tableName, entityClass = HisMsg::class/*, schema = "hismsg"*/) {
    val id: Column<Long> = long("id").bindTo { it.id }.primaryKey()
    val type: Column<String> = varchar("type").bindTo { it.type }
    val msg: Column<String> = varchar("msg").bindTo { it.msg }
    val role: Column<Int> = int("role").bindTo { it.role }

    companion object {
        val Instence = THisMsg("HisMsg")
    }
}
