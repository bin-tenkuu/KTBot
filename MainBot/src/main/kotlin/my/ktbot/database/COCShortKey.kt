package my.ktbot.database

import my.ktbot.utils.global.databaseGlobal
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.update
import org.ktorm.entity.Entity
import org.ktorm.entity.removeIf
import org.ktorm.entity.sequenceOf
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

    val value: Column<String> = varchar("value").bindTo { it.value }

    val all = HashMap<String, String>()

    init {
        for (cocShortKey in databaseGlobal.sequenceOf(TCOCShortKey)) {
            all[cocShortKey.key] = cocShortKey.value
        }
    }

    fun set(key: String, value: String) {
        if (key in all) {
            databaseGlobal.update(TCOCShortKey) {
                it.value to value
                where {
                    it.key eq key
                }
            }
        } else {
            databaseGlobal.insert(TCOCShortKey) {
                set(it.key, key)
                set(it.value, value)
            }
        }
        all[key] = value
    }

    fun remove(key: String) {
        databaseGlobal.sequenceOf(TCOCShortKey).removeIf {
            it.key eq key
        }
    }
}
