package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.ksp.api.Column
import org.ktorm.ksp.api.PrimaryKey
import org.ktorm.ksp.api.Table

@Table(tableName = "TodayInHistory", tableClassName = "TTodayInHistory", alias = "tih")
interface TodayInHistory : Entity<TodayInHistory> {
	@PrimaryKey
	val autoid: Long
	var month: Int
	var day: Int
	var date: String

	@Column(columnName = "e_id")
	var eId: Int
	var title: String
	var content: String?

	companion object : Entity.Factory<TodayInHistory>()
}
