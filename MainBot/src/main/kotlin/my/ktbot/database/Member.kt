package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.Instant
import java.time.LocalDateTime

interface Member : Gmt<Member> {
    var name: String

    companion object : Entity.Factory<Member>()
}

object TMember : GmtTable<Member>(tableName = "qq_mamber", entityClass = Member::class) {
    val name: Column<String> = varchar("name").bindTo { it.name }
}
