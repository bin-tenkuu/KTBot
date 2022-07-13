package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.ksp.api.PrimaryKey
import org.ktorm.ksp.api.Table

/**
 *
 * @author bin
 * @since 2022/1/6
 */

@Table(tableName = "PixivPic", tableClassName = "TPixivPic")
interface PixivPic : Entity<PixivPic> {
	var id: Long

	@PrimaryKey
	var pid: Int

	@PrimaryKey
	var p: Int
	var uid: Int
	var r18: Boolean
	var url: String
	var author: String
	var title: String

	companion object : Entity.Factory<PixivPic>()
}
