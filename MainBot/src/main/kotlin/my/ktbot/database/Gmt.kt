package my.ktbot.database

import org.ktorm.entity.Entity

interface Gmt<T : Gmt<T>> : Entity<T> {
	var id: Long
	var exp: Double

	var gmtModified: Long

	val gmtCreate: Long

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

