package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.ksp.api.Table

/**
 *
 * @author bin
 * @since 2022/1/6
 */

@Table(tableName = "QQGroup", tableClassName = "TGroup", alias = "g")
interface Group : Gmt<Group> {
	var invited: Long

	companion object : Entity.Factory<Group>()
}
