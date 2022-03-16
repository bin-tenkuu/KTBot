package my.ktbot.plugin.plugs.subPlugs

import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.database.TCOCShortKey
import my.ktbot.plugin.utils.Sqlite
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.ktorm.entity.toList

/**
 *
 * @author bin
 * @since 2022/1/7
 */
object COCStat : Plug(
	name = "骰子：简写",
	regex = Regex("^[.．。]dstat$", RegexOption.IGNORE_CASE),
	weight = 1.01,
	help = "查看全部简写".toPlainText(),
	msgLength = 5..7,
) {
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message {
		val list = Sqlite[TCOCShortKey].toList()
		return (if (list.isEmpty()) "空"
		else list.joinToString("\n") { sk ->
			"${sk.key}=${sk.value}"
		}).toPlainText()
	}
}
