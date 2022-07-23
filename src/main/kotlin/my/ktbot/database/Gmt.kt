package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.ksp.api.Column
import org.ktorm.ksp.api.PrimaryKey

interface Gmt<T : Gmt<T>> : Entity<T> {
	@PrimaryKey
	var id: Long
	var exp: Double

	@Column(columnName = "gmt_modified")
	var gmtModified: Long

	@Column(columnName = "gmt_create")
	var gmtCreate: Long

	@Column(columnName = "is_baned")
	var isBaned: Boolean

	companion object {
		fun Gmt<*>.modify() {
			gmtModified = System.currentTimeMillis()
		}

		fun Gmt<*>.add(exp: Double): Boolean {
			if (exp < 0 && this.exp < -exp) return false
			modify()
			this.exp = this.exp + exp; return true
		}

		inline fun <T : Gmt<T>> T.update(block: (T.() -> Unit) = {}) {
			block()
			modify()
			flushChanges()
		}
	}
}

