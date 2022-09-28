package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.ksp.api.PrimaryKey
import org.ktorm.ksp.api.Table

@Table(tableName = "PermCheck", tableClassName = "TPermCheck", alias = "pc")
interface PermCheck : Entity<PermCheck> {
	@PrimaryKey
	var id: Long

	/**
	 * 使用[my.miraiplus.Caller.name]
	 */
	var name: String

	companion object : Entity.Factory<PermCheck>()
}
