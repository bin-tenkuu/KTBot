package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.ksp.api.PrimaryKey
import org.ktorm.ksp.api.Table

/**
 * @since 2022/1/7
 * @author bin
 */
@Table(tableName = "COCShortKey", tableClassName = "TCOCShortKey")
interface COCShortKey : Entity<COCShortKey> {
	@PrimaryKey
	var key: String
	var value: String

	companion object : Entity.Factory<COCShortKey>()
}

