package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.*

interface TodayInHistory : Entity<TodayInHistory> {
    val id: Long
    var month: Int
    var day: Int
    var date: String
    var eId: Int
    var title: String
    var content: String?

    companion object : Entity.Factory<TodayInHistory>()
}

object TTodayInHistory : Table<TodayInHistory>(tableName = "today_in_history", entityClass = TodayInHistory::class) {
    val id: Column<Long> = long("id").bindTo { it.id }.primaryKey()
    val month: Column<Int> = int("month").bindTo { it.month }
    val day: Column<Int> = int("day").bindTo { it.day }
    val date: Column<String> = varchar("date").bindTo { it.date }
    val eId: Column<Int> = int("e_id").bindTo { it.eId }
    val title: Column<String> = varchar("title").bindTo { it.title }
    val content: Column<String> = varchar("content").bindTo { it.content }
}
