package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.ksp.api.Table

/**
 *  @Date:2022/7/24
 *  @author bin
 *  @version 1.0.0
 */
@Table(tableName = "JeffJoke", tableClassName = "TJeffJoke")
interface JeffJoke : Entity<JeffJoke> {
	val id: Int
	val text: String

	companion object : Entity.Factory<Group>()
}
