package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.Column
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/**
 *  @Date:2022/7/24
 *  @author bin
 *  @version 1.0.0
 */
interface JeffJoke : Entity<JeffJoke> {
    val id: Int
    val text: String

    companion object : Entity.Factory<Group>()
}

object TJeffJoke : Table<JeffJoke>(tableName = "jeff_joke", entityClass = JeffJoke::class) {
    val id: Column<Int> = int("id").bindTo { it.id }.primaryKey()
    val text: Column<String> = varchar("text").bindTo { it.text }
}
