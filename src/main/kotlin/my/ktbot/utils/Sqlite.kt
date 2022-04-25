package my.ktbot.utils

import my.ktbot.PluginMain
import org.ktorm.database.Database
import org.ktorm.dsl.AssignmentsBuilder
import org.ktorm.dsl.KtormDsl
import org.ktorm.entity.EntitySequence
import org.ktorm.entity.sequenceOf
import org.ktorm.expression.*
import org.ktorm.schema.*
import org.ktorm.support.sqlite.SQLiteDialect
import org.ktorm.support.sqlite.SQLiteFormatter
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
		dialect = SQLiteCostom,
		logger = LoggerBridge(PluginMain.logger),
		alwaysQuoteIdentifiers = true,
		generateSqlInUpperCase = true
	)

	@JvmStatic
	operator fun <E : Any, T : BaseTable<E>> get(table: T): EntitySequence<E, T> {
		return database.sequenceOf(table)
	}

	@JvmStatic
	val random by lazy { FunctionExpression("random", emptyList(), LongSqlType, true) }

	@JvmStatic
	operator fun invoke(boolean: Boolean): ArgumentExpression<Boolean> {
		return ArgumentExpression(boolean, BooleanSqlType)
	}

}
// region 自定义sql

object SQLiteCostom : SQLiteDialect() {

	override fun createSqlFormatter(database: Database, beautifySql: Boolean, indentSize: Int): SqlFormatter {
		return SQLiteFormatterCostom(database, beautifySql, indentSize)
	}

}

class SQLiteFormatterCostom(
	database: Database, beautifySql: Boolean, indentSize: Int,
) : SQLiteFormatter(database, beautifySql, indentSize) {
	override fun visit(expr: SqlExpression): SqlExpression {
		return when (expr) {
			is InsertOrUpdateExpression -> visitInsertOrUpdate(expr)
			else -> super.visit(expr)
		}
	}

	private fun visitInsertOrUpdate(expr: InsertOrUpdateExpression): InsertOrUpdateExpression {
		writeKeyword("insert into ")
		visitTable(expr.table)
		writeInsertColumnNames(expr.assignments.map(ColumnAssignmentExpression<*>::column))
		writeKeyword("values ")
		writeInsertValues(expr.assignments)
		if (expr.conflictColumns.isNotEmpty()) {
			writeKeyword("on conflict ")
			writeInsertColumnNames(expr.conflictColumns)
			if (expr.updateAssignments.isNotEmpty()) {
				writeKeyword("do update set ")
				visitColumnAssignments(expr.updateAssignments)
			}
			else writeKeyword("do nothing ")
		}
		return expr
	}
}

data class InsertOrUpdateExpression(
	val table: TableExpression,
	val assignments: List<ColumnAssignmentExpression<*>>,
	val conflictColumns: List<ColumnExpression<*>>,
	val updateAssignments: List<ColumnAssignmentExpression<*>> = assignments,
	override val isLeafNode: Boolean = false,
	override val extraProperties: Map<String, Any> = emptyMap(),
) : SqlExpression()


/**
 * DSL builder for insert or update statements.
 */
@KtormDsl
class InsertOrUpdateStatementBuilder : AssignmentsBuilder() {
	val assignments: List<ColumnAssignmentExpression<*>> get() = _assignments
	internal val conflictColumns = ArrayList<Column<*>>()
	internal val updateAssignments = ArrayList<ColumnAssignmentExpression<*>>()
	internal var doNothing = false

	/**
	 * Specify the update assignments while any key conflict exists.
	 */
	fun onConflict(vararg columns: Column<*>, block: OnConflictClauseBuilder.() -> Unit) {
		val builder = OnConflictClauseBuilder().apply(block)
		this.conflictColumns += columns
		this.updateAssignments += builder.assignments
		this.doNothing = builder.doNothing
	}

	/**
	 * DSL builder for insert or update on conflict clause.
	 */
	@KtormDsl
	class OnConflictClauseBuilder : AssignmentsBuilder() {
		internal val assignments: List<ColumnAssignmentExpression<*>> get() = _assignments
		internal var doNothing = false

		/**
		 * Explicitly tells ktorm to ignore any on-conflict errors and continue insertion.
		 */
		fun doNothing() {
			this.doNothing = true
		}

		/**
		 * Reference the 'EXCLUDED' table in a ON CONFLICT clause.
		 */
		fun <T : Any> excluded(column: Column<T>): ColumnExpression<T> {
			// excluded.name
			return ColumnExpression(
				table = TableExpression(name = "excluded"),
				name = column.name,
				sqlType = column.sqlType
			)
		}

		fun <T : Any> setExcluded(column: Column<T>) = set(column, excluded(column))
	}

}

private fun <T : BaseTable<*>> buildInsertOrUpdateExpression(
	table: T, block: InsertOrUpdateStatementBuilder.(T) -> Unit,
): InsertOrUpdateExpression {
	val builder = InsertOrUpdateStatementBuilder().apply { block(table) }
	val conflictColumns = builder.conflictColumns.ifEmpty(table::primaryKeys)
	if (conflictColumns.isEmpty()) {
		val msg = "Table '$table' doesn't have a primary key, " +
			"you must specify the conflict columns when calling onConflict(col) { .. }"
		throw IllegalStateException(msg)
	}
	if (!builder.doNothing && builder.updateAssignments.isEmpty()) {
		val msg = "Cannot leave the onConflict clause empty! " +
			"If you desire no update action at all please explicitly call `doNothing()`"
		throw IllegalStateException(msg)
	}
	return InsertOrUpdateExpression(
		table.asExpression(),
		builder.assignments,
		conflictColumns.map(Column<*>::asExpression),
		if (builder.doNothing) emptyList() else builder.updateAssignments
	)
}

fun <T : BaseTable<*>> Database.insertOrUpdate(
	table: T,
	block: InsertOrUpdateStatementBuilder.(T) -> Unit,
): Int {
	val expression = SQLiteFormatterCostom(this, false, 0)
		.visit(buildInsertOrUpdateExpression(table, block))
	return executeUpdate(expression)
//	return 1
}

fun <E : Any, T : BaseTable<E>> EntitySequence<E, T>.insertOrUpdate(
	block: InsertOrUpdateStatementBuilder.(T) -> Unit
): Int {
	return database.insertOrUpdate(sourceTable, block)
}

//public fun <T : BaseTable<*>> T.insertOrUpdate(block: InsertOrUpdateStatementBuilder.(T) -> Unit): Int {
//	return Database.global.insertOrUpdate(this, block)
//}
// endregion
