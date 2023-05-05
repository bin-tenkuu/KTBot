package my.ktbot.plugs

import my.ktbot.database.TCOCShortKey
import my.ktbot.service.CocService.dice
import my.ktbot.utils.Sqlite
import my.miraiplus.NeverEvent
import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import kotlin.text.RegexOption.IGNORE_CASE

/**
 *  @since:2022/1/2
 *  @author bin
 */
object CQBotCOC {
    @JvmStatic
    private val diceRegex = Regex("[^+\\-*d\\d#]", IGNORE_CASE)

    @MiraiEventHandle("骰子主功能", eventType = NeverEvent::class)
    @RegexAnn("^.d +(?:(?<times>\\d)#)?(?<dice>[^ ]+)", IGNORE_CASE)
    fun invoke(groups: MatchGroupCollection): Message? {
        val times: Int = groups["times"]?.run { value.trim().toIntOrNull() } ?: 1
        var dice: String = groups["dice"]?.value ?: return null

        for (sk in Sqlite[TCOCShortKey]) {
            dice = dice.replace(sk.key, sk.value, true)
        }

        if (diceRegex.matches(dice)) {
            return ".d错误参数".toPlainText()
        }
        val str = Array(times) { dice(dice, 0) }.joinToString("\n")
        return str.toPlainText()
    }

}
