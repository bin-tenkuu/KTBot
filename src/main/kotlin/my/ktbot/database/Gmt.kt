package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

object StringSetSqlType : SqlType<MutableSet<String>>(Types.VARCHAR, "Set<String>") {
	override fun doGetResult(rs: ResultSet, index: Int): MutableSet<String> {
		return rs.getString(index).split(',').toMutableSet()
	}

	override fun doSetParameter(ps: PreparedStatement, index: Int, parameter: MutableSet<String>) {
		ps.setString(index, parameter.joinToString(",") { it })
	}
}

abstract class TGmt<T : Gmt<T>>(tableName: String) : Table<T>(tableName) {
	val id = long("id").primaryKey().bindTo { it.id }
	val exp = double("exp").bindTo { it.exp }
	val gmtModified = long("gmt_modified").bindTo { it.gmtModified }
	val gmtCreate = long("gmt_create").bindTo { it.gmtCreate }
	val isBaned = boolean("is_baned").bindTo { it.isBaned }
	val permGroup = registerColumn("perm_group", StringSetSqlType).bindTo { it.permGroup }
	val permEX = registerColumn("perm_ex", StringSetSqlType).bindTo { it.permEX }
}

interface Gmt<T : Gmt<T>> : Entity<T> {
	var id: Long
	var gmtModified: Long
	var gmtCreate: Long
	var exp: Double
	var isBaned: Boolean
	var permGroup: MutableSet<String>
	var permEX: MutableSet<String>
}

infix fun <T : Gmt<T>> T.add(exp: Double): Boolean {
	modify()
	if (this.exp < -exp) return false
	this.exp = this.exp + exp; return true
}

fun <T : Gmt<T>> T.modify() {
	gmtModified = System.currentTimeMillis()
}

