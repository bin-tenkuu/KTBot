package my.ktbot.plugin.plugs.subPlugs

import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.database.COCShortKey
import my.ktbot.plugin.database.TCOCShortKey
import my.ktbot.plugin.utils.Sqlite
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.removeIf

/**
 *
 * @author bin
 * @since 2022/1/7
 */
object COCStatSet : Plug(
	name = "骰子：删除[设置]简写",
	regex = Regex("^[.．。]dset +(?<key>\\w[\\w\\d]+)(?:=(?<value>[+\\-*d0-9#]+))?", RegexOption.IGNORE_CASE),
	weight = 1.02,
	help = "删除[设置]简写".toPlainText(),
	msgLength = 5..100,
) {
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message {
		val key = result["key"]?.value
		val value = result["value"]?.value
		if (key === null || key.length > 5) {
			return "key格式错误或长度大于5".toPlainText()
		}
		val shortKey = Sqlite[TCOCShortKey]
		if (value === null) {
			shortKey.removeIf { it.key eq key }
			return "删除key:${key}".toPlainText()
		}
		if (value.length > 10) {
			return "value长度不大于10".toPlainText()
		}
		shortKey.add(COCShortKey {
			this.key = key
			this.value = value
		})
		return "添加key:${key}=${value}".toPlainText()
	}
}
