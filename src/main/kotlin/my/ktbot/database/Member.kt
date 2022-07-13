package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.ksp.api.Table

@Table(tableName = "Members", tableClassName = "TMember")
interface Member : Gmt<Member> {
	var name: String

	companion object : Entity.Factory<Member>()
}
