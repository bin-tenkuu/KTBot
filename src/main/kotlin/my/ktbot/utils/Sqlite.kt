package my.ktbot.utils

import my.ktbot.PluginMain
import org.ktorm.database.Database
import org.ktorm.entity.*
import org.ktorm.expression.ArgumentExpression
import org.ktorm.schema.*
import org.ktorm.support.sqlite.*
import kotlin.io.path.div

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/10
 */
object Sqlite {
	@JvmStatic
	val database: Database = Database.connect(
		url = "jdbc:sqlite:${PluginMain.dataFolderPath / "../db.db"}",
		driver = "org.sqlite.JDBC",
		user = null,
		password = null,
		dialect = SQLiteDialect(),
		logger = LoggerBridge(PluginMain.logger),
		alwaysQuoteIdentifiers = true,
		generateSqlInUpperCase = true
	)

	@JvmStatic
	operator fun <E : Entity<E>, T : BaseTable<E>> get(table: T): EntitySequence<E, T> {
		return database.sequenceOf(table)
	}

	@JvmStatic
	val random = random()

	@JvmStatic
	operator fun invoke(boolean: Boolean): ArgumentExpression<Boolean> {
		return ArgumentExpression(boolean, BooleanSqlType)
	}

	fun <E : Any, T : BaseTable<E>> EntitySequence<E, T>.insertOrUpdate(
		block: InsertOrUpdateStatementBuilder.(T) -> Unit,
	): Int {
		return database.insertOrUpdate(sourceTable, block)
	}

	fun <E : Any, T : BaseTable<E>> EntitySequence<E, T>.limit(
		limit: Int,
	): EntitySequence<E, T> {
		return withExpression(expression.copy(limit = limit))
	}

	fun <T : Any> InsertOrUpdateOnConflictClauseBuilder.setExcluded(column: Column<T>) = set(column, excluded(column))

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
