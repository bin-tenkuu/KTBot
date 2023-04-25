package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.*

/**
 *
 * @author bin
 * @since 2022/1/6
 */

interface PixivPic : Entity<PixivPic> {
    val id: Long
    val pid: Int
    val p: Int
    val uid: Int
    val r18: Boolean
    val url: String
    val author: String
    val title: String

    companion object : Entity.Factory<PixivPic>()
}

object TPixivPic : Table<PixivPic>(tableName = "pixiv_pic", entityClass = PixivPic::class) {
    val id: Column<Long> = long("id").bindTo { it.id }
    val pid: Column<Int> = int("pid").bindTo { it.pid }.primaryKey()
    val p: Column<Int> = int("p").bindTo { it.p }.primaryKey()
    val uid: Column<Int> = int("uid").bindTo { it.uid }
    val r18: Column<Boolean> = boolean("r18").bindTo { it.r18 }
    val url: Column<String> = varchar("url").bindTo { it.url }
    val author: Column<String> = varchar("author").bindTo { it.author }
    val title: Column<String> = varchar("title").bindTo { it.title }
}
