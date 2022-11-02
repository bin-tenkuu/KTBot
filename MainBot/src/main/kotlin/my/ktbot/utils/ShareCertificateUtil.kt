package my.ktbot.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal

/**
 * 股票
 */
object ShareCertificateUtil {
	private val logger = createLogger<Counter>()
	private val amStart = LocalTime.of(9, 30)
	private val amEnd = LocalTime.of(11, 30)
	private val pmStart = LocalTime.of(13, 0)
	private val pmEnd = LocalTime.of(15, 0)

	fun start(scope: CoroutineScope) {
		scope.launch {
			listOf("sh600536", "sz159949", "sZ159949").also {
				val map = invoke(it)
				for (code in it) {
					val data = map[code] ?: continue
					logger.info(
						"名称:${data.name},代码:${data.code},当前价格:${
							data.currentPrice
						},涨额:${data.raise},涨辐:${data.raiseRange}"
					)
				}
			}
			while (true) {
				// 如果没找到bot，每秒检查一次
				delay(1000)
				val bot = Bot.instances.firstOrNull() ?: continue
				// 延时到开盘或8分钟后
				delay(nextShareDelay)
				while (bot.isOnline) {
					// 延时8分钟到下一次开盘
					delay(nextShareDelay)
				}
			}
		}.invokeOnCompletion {
			logger.warning("股票监视下线")
		}
	}

	private operator fun Temporal.minus(late: Temporal) = until(late, ChronoUnit.MILLIS)

	private val nextShareDelay: Long
		get() {
			val now = LocalDateTime.now()
			val time = now.toLocalTime()
			when {
				time < amStart -> return time - amStart
				time < amEnd -> {
					val next = time.plusMinutes(8)
					if (next < amEnd) {
						return time - next
					}
					return time - pmStart
				}
				time < pmStart -> return time - pmStart
				time < pmEnd -> {
					val next = time.plusMinutes(8)
					if (next < pmEnd) {
						return time - next
					}
					return now - now.with(amStart).plusDays(1)
				}
				else -> return now - now.with(amStart).plusDays(1)
			}
		}

	// 腾讯api：https://blog.csdn.net/qq_32531329/article/details/121260138
	// 聚合api：https://dashboard.juhe.cn/data/index/my

	suspend operator fun invoke(shareCode: List<String>): Map<String, ShareData?> = Gtimg(shareCode)

	/**
	 * 股票信息
	 * @property name [String] 名称
	 * @property code [Int] 代码
	 * @property currentPrice [Float] 当前价格
	 * @property raise [Float] 涨额
	 * @property raiseRange [Float] 涨辐
	 * @constructor
	 */
	class ShareData(
		val name: String,
		val code: Int,
		val currentPrice: Float,
		val raise: Float,
		val raiseRange: Float,
	)

	private interface ShareDataApi : suspend (List<String>) -> Map<String, ShareData?> {
		override suspend fun invoke(shareCode: List<String>): Map<String, ShareData?>
	}

	/**
	 * 腾讯
	 */
	private object Gtimg : ShareDataApi {
		override suspend fun invoke(shareCode: List<String>): Map<String, ShareData?> {
			@Suppress("HttpUrlsUsage")
			val url = shareCode.joinToString(",", "http://qt.gtimg.cn/q=") { "s_$it" }
			val body = KtorUtils.get(url).body<String>().split("\";").take(shareCode.size)
			return body.asSequence().map { it.split("~") }.filter { it.size >= 6 }.associateBy({
				it[0].substring(4, 12)
			}) {
				ShareData(
					it[1],
					it[2].toIntOrNull() ?: return@associateBy null,
					it[3].toFloatOrNull() ?: return@associateBy null,
					it[4].toFloatOrNull() ?: return@associateBy null,
					it[5].toFloatOrNull() ?: return@associateBy null,
				)
			}
		}
	}
}
