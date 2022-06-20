package my.ktbot.utils

import my.ktbot.PluginMain
import org.ktorm.database.Database
import org.ktorm.entity.EntitySequence
import org.ktorm.entity.sequenceOf
import org.ktorm.expression.ArgumentExpression
import org.ktorm.expression.FunctionExpression
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
	operator fun <E : Any, T : BaseTable<E>> get(table: T): EntitySequence<E, T> {
		return database.sequenceOf(table)
	}

	@JvmStatic
	val random = FunctionExpression("random", emptyList(), LongSqlType, true)

	@JvmStatic
	operator fun invoke(boolean: Boolean): ArgumentExpression<Boolean> {
		return ArgumentExpression(boolean, BooleanSqlType)
	}

	fun <E : Any, T : BaseTable<E>> EntitySequence<E, T>.insertOrUpdate(
		block: InsertOrUpdateStatementBuilder.(T) -> Unit,
	): Int {
		return database.insertOrUpdate(sourceTable, block)
	}

	fun <T : Any> InsertOrUpdateOnConflictClauseBuilder.setExcluded(column: Column<T>) = set(column, excluded(column))

}
