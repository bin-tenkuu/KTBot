package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.*

interface Member : Gmt<Member> {
    var name: String

    companion object : Entity.Factory<Member>()
}

object TMember : Table<Member>(tableName = "qq_mamber", entityClass = Member::class) {
    val id: Column<Long> = long("id").bindTo { it.id }.primaryKey()
    val name: Column<String> = varchar("name").bindTo { it.name }
    val exp: Column<Double> = double("exp").bindTo { it.exp }
    val gmtModified: Column<Long> = long("gmt_modified").bindTo { it.gmtModified }
    val gmtCreate: Column<Long> = long("gmt_create").bindTo { it.gmtCreate }
    val isBaned: Column<Boolean> = boolean("is_baned").bindTo { it.isBaned }
}
