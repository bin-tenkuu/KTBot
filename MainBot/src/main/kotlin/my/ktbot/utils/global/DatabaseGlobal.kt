package my.ktbot.utils.global

import my.ktbot.PluginMain
import net.mamoe.mirai.utils.MiraiLogger
import org.ktorm.database.Database
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel
import org.ktorm.logging.Logger
import org.ktorm.support.sqlite.SQLiteDialect
import kotlin.io.path.div

/**
 * @author bin
 * @since 2023/04/24
 */
val databaseGlobal: Database = Database.connect(
    url = "jdbc:sqlite:" + Debugger.yes("./data/db.db") { PluginMain.dataFolderPath / "../db.db" },
    driver = "org.sqlite.JDBC",
    user = null,
    password = null,
    dialect = SQLiteDialect(),
    logger = Debugger.yes(ConsoleLogger(threshold = LogLevel.DEBUG)) { LoggerBridge(PluginMain.logger) },
    alwaysQuoteIdentifiers = true,
    generateSqlInUpperCase = true
)

private class LoggerBridge(private val logger: MiraiLogger) : Logger {
    override fun debug(msg: String, e: Throwable?) = logger.debug(msg, e)
    override fun error(msg: String, e: Throwable?) = logger.error(msg, e)
    override fun info(msg: String, e: Throwable?) = logger.info(msg, e)
    override fun isDebugEnabled(): Boolean = logger.isDebugEnabled
    override fun isErrorEnabled(): Boolean = logger.isErrorEnabled
    override fun isInfoEnabled(): Boolean = logger.isInfoEnabled
    override fun isTraceEnabled(): Boolean = logger.isVerboseEnabled
    override fun isWarnEnabled(): Boolean = logger.isWarningEnabled
    override fun trace(msg: String, e: Throwable?) = logger.verbose(msg, e)
    override fun warn(msg: String, e: Throwable?) = logger.warning(msg, e)
}
