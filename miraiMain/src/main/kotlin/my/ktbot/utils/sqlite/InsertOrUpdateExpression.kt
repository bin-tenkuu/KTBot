package my.ktbot.utils.sqlite

import org.ktorm.expression.*

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/10
 */
data class InsertOrUpdateExpression(
	val table: TableExpression,
	val assignments: List<ColumnAssignmentExpression<*>>,
	val conflictColumns: List<ColumnExpression<*>>,
	val updateAssignments: List<ColumnAssignmentExpression<*>> = assignments,
	override val isLeafNode: Boolean = false,
	override val extraProperties: Map<String, Any> = emptyMap(),
) : SqlExpression()
