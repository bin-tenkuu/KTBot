package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.ksp.api.PrimaryKey
import org.ktorm.ksp.api.Table

/**
 *
 * @author bin
 * @since 2022/1/6
 */

@Table(tableName = "PixivPic", tableClassName = "TPixivPic", alias = "pp")
interface PixivPic : Entity<PixivPic> {
	val id: Long

	@PrimaryKey
	val pid: Int

	@PrimaryKey
	val p: Int
	val uid: Int
	val r18: Boolean
	val url: String
	val author: String
	val title: String

	companion object : Entity.Factory<PixivPic>()
}
