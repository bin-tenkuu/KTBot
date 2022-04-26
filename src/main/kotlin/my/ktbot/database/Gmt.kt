package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.*

abstract class TGmt<T : Gmt<T>>(tableName: String) : Table<T>(tableName) {
	val id = long("id").primaryKey().bindTo { it.id }
	val exp = double("exp").bindTo { it.exp }
	val gmtModified = long("gmt_modified").bindTo { it.gmtModified }
	val gmtCreate = long("gmt_create").bindTo { it.gmtCreate }
	val isBaned = boolean("is_baned").bindTo { it.isBaned }
}

interface Gmt<T : Gmt<T>> : Entity<T> {
	var id: Long
	var gmtModified: Long
	var gmtCreate: Long
	var exp: Double
	var isBaned: Boolean
}

infix fun <T : Gmt<T>> T.add(exp: Double): Boolean {
	if (exp < 0 && this.exp < -exp) return false
	modify()
	this.exp = this.exp + exp; return true
}

fun <T : Gmt<T>> T.modify() {
	gmtModified = System.currentTimeMillis()
}

