package my.ktbot.ktor.dao

import io.ktor.server.websocket.*
import kotlinx.serialization.Serializable
import my.ktbot.PluginMain
import org.ktorm.database.Database
import org.ktorm.support.sqlite.SQLiteDialect
import kotlin.io.path.div

/**
 * @author bin
 * @version 1.0.0
 * @Date:2023/3/12
 */
class RoomConfig(
	val name: String,
) {
	val roles = HashMap<String?, MutableList<Tag>>()
	val clients = HashSet<DefaultWebSocketServerSession>()
	val dataSource: Database = Database.connect(
		url = "jdbc:sqlite:${PluginMain.dataFolderPath / "${name}.db"}",
		driver = "org.sqlite.JDBC",
		dialect = SQLiteDialect(),
		alwaysQuoteIdentifiers = true,
		generateSqlInUpperCase = true
	)

	suspend fun sendAll(msg: Message) {
		for (client in clients) {
			client.sendSerialized(msg)
		}
	}
}

@Serializable
class Tag(val key: String, val color: String)
