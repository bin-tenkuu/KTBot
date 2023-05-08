package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.LocalDateTime
import kotlin.reflect.KClass

interface Gmt<T : Gmt<T>> : Entity<T> {
    var id: Long
    var exp: Double
    var gmtModified: LocalDateTime
    val gmtCreate: LocalDateTime
    var isBaned: Boolean

    companion object {
        fun Gmt<*>.modify() {
            gmtModified = LocalDateTime.now()
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

open class GmtTable<T : Gmt<T>>(tableName: String, entityClass: KClass<T>) : Table<T>(tableName = tableName, entityClass = entityClass) {
    val id: Column<Long> = long("id").bindTo { it.id }.primaryKey()
    val exp: Column<Double> = double("exp").bindTo { it.exp }
    val gmtModified: Column<LocalDateTime> = datetime("gmt_modified").bindTo { it.gmtModified }
    val gmtCreate: Column<LocalDateTime> = datetime("gmt_create").bindTo { it.gmtCreate }
    val isBaned: Column<Boolean> = boolean("is_baned").bindTo { it.isBaned }
}
