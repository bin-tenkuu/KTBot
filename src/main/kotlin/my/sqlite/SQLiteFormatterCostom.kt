package my.sqlite

import org.ktorm.database.Database
import org.ktorm.expression.ColumnAssignmentExpression
import org.ktorm.expression.SqlExpression
import org.ktorm.support.sqlite.SQLiteFormatter

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/10
 */
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
