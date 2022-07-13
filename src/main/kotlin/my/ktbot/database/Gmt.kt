package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.ksp.api.PrimaryKey

interface Gmt<T : Gmt<T>> : Entity<T> {
	@PrimaryKey
	var id: Long
	var exp: Double
	var gmtModified: Long
	var gmtCreate: Long
	var isBaned: Boolean

	companion object {
		infix fun <T : Gmt<T>> Gmt<T>.add(exp: Double): Boolean {
			if (exp < 0 && this.exp < -exp) return false
			modify()
			this.exp = this.exp + exp; return true
		}

		fun <T : Gmt<T>> Gmt<T>.modify() {
			gmtModified = System.currentTimeMillis()
		}
	}
}

