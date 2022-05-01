package my.ktbot.utils.sqlite

import org.ktorm.database.Database
import org.ktorm.expression.SqlFormatter
import org.ktorm.support.sqlite.SQLiteDialect

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/10
 */
object SQLiteCostom : SQLiteDialect() {

	override fun createSqlFormatter(database: Database, beautifySql: Boolean, indentSize: Int): SqlFormatter {
		return SQLiteFormatterCostom(database, beautifySql, indentSize)
	}

}
