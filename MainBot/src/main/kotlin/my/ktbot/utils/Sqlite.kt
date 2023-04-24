package my.ktbot.utils

import my.ktbot.utils.global.databaseGlobal
import org.ktorm.entity.*
import org.ktorm.expression.ArgumentExpression
import org.ktorm.schema.BaseTable
import org.ktorm.schema.BooleanSqlType
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.schema.Table
import org.ktorm.support.sqlite.InsertOrUpdateStatementBuilder
import org.ktorm.support.sqlite.insertOrUpdate
import org.ktorm.support.sqlite.random

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/10
 */
object Sqlite {

    @JvmStatic
    operator fun <E : Entity<E>, T : BaseTable<E>> get(table: T): EntitySequence<E, T> {
        return databaseGlobal.sequenceOf(table)
    }

    @JvmStatic
    val random = random()

    @JvmStatic
    operator fun invoke(boolean: Boolean): ArgumentExpression<Boolean> {
        return ArgumentExpression(boolean, BooleanSqlType)
    }

    fun <E : Any, T : BaseTable<E>> insertOrUpdate(
        sourceTable: T,
        block: InsertOrUpdateStatementBuilder.(T) -> Unit,
    ): Int {
        return databaseGlobal.insertOrUpdate(sourceTable, block)
    }

    fun <E : Any, T : BaseTable<E>> EntitySequence<E, T>.limit(
        limit: Int,
    ): EntitySequence<E, T> {
        return withExpression(expression.copy(limit = limit))
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <E : Entity<E>, T : Table<E>> EntitySequence<E, T>.findOrAdd(
        predicate: (T) -> ColumnDeclaring<Boolean>,
        block: E.() -> Unit,
    ): E {
        return firstOrNull(predicate) ?: (Entity.create(sourceTable.entityClass!!) as E).also {
            block(it)
            add(it)
        }
    }
}
