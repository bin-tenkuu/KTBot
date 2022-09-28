package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.ksp.api.PrimaryKey
import org.ktorm.ksp.api.Table

/**
 * @since 2022/1/7
 * @author bin
 */
@Table(tableName = "COCShortKey", tableClassName = "TCOCShortKey", alias = "csk")
interface COCShortKey : Entity<COCShortKey> {
	@PrimaryKey
	val key: String
	val value: String

	companion object : Entity.Factory<COCShortKey>()
}

