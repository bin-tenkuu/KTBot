package my.ktbot.plugin.database

import org.ktorm.entity.Entity
import org.ktorm.schema.varchar

object TMembers : TGmt<Members>("Members") {
	val name = varchar("name").bindTo { it.name }
}

interface Members : Gmt<Members> {
	var name: String

	companion object : Entity.Factory<Members>()
}
