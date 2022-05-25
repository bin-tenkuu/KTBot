package my.sqlite

import org.ktorm.database.Database
import org.ktorm.dsl.AssignmentsBuilder
import org.ktorm.dsl.KtormDsl
import org.ktorm.entity.EntitySequence
import org.ktorm.expression.ColumnAssignmentExpression
import org.ktorm.expression.ColumnExpression
import org.ktorm.expression.TableExpression
import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column

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
