package my.ktbot.plugin.database

import org.ktorm.entity.Entity
import org.ktorm.schema.*

/**
 *
 * @author bin
 * @since 2022/1/6
 */

object TPixivPic : Table<PixivPic>("PixivPic") {
	val id = long("id").bindTo { it.id }
	val pid = int("pid").primaryKey().bindTo { it.pid }
	val p = int("p").primaryKey().bindTo { it.p }
	val uid = int("uid").bindTo { it.uid }
	val r18 = boolean("r18").bindTo { it.r18 }
	val url = varchar("url").bindTo { it.url }
	val author = varchar("author").bindTo { it.author }
	val title = varchar("title").bindTo { it.title }
}

interface PixivPic : Entity<PixivPic> {
	var id: Long
	var pid: Int
	var p: Int
	var uid: Int
	var r18: Boolean
	var url: String
	var author: String
	var title: String

	companion object : Entity.Factory<PixivPic>()
}