package my.ktbot.plugin.database

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

/**
 * @since 2022/1/7
 * @author bin
 */
object TCQCShortKey : Table<COCShortKey>("COCShortKey") {
	val key = varchar("key").primaryKey().bindTo { it.key }
	val value = varchar("value").bindTo { it.value }
}

interface COCShortKey : Entity<COCShortKey> {
	var key: String
	var value: String

	companion object : Entity.Factory<COCShortKey>()
}