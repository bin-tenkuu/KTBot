package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.Column
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

/**
 * @since 2022/1/7
 * @author bin
 */
interface COCShortKey : Entity<COCShortKey> {
    val key: String
    val value: String

    companion object : Entity.Factory<COCShortKey>()
}


object TCOCShortKey : Table<COCShortKey>(tableName = "coc_short_key", entityClass = COCShortKey::class) {
    val key: Column<String> = varchar("key").bindTo { it.key }.primaryKey()

    val `value`: Column<String> = varchar("value").bindTo { it.value }
}
