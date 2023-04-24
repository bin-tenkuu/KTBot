package my.ktbot.service

import kotlinx.coroutines.runBlocking
import my.ktbot.PlugConfig
import my.ktbot.dao.juhe.JuheBase
import my.ktbot.dao.juhe.JuheTodayInHistory
import my.ktbot.dao.juhe.JuheTodayInHistoryDetail
import my.ktbot.database.TTodayInHistory
import my.ktbot.database.TodayInHistory
import my.ktbot.utils.KtorUtils
import my.ktbot.utils.Sqlite
import my.ktbot.utils.Sqlite.limit
import my.ktbot.utils.Sqlite.random
import my.ktbot.utils.global.databaseGlobal
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.isEmpty
import org.ktorm.entity.sortedBy
import org.ktorm.entity.toList
import org.ktorm.support.sqlite.bulkInsertOrUpdate
import java.time.LocalDate

object TodayInHistoryService {
	private val spaceRegEx = Regex("\\n[\\s\\n]+")

	fun getRandom(date: LocalDate = LocalDate.now(), limit: Int = 1): List<TodayInHistory> {
		if (limit < 1) {
			return emptyList()
		}
		val sequence = Sqlite[TTodayInHistory].filter {
			(it.month eq date.monthValue) and (it.day eq date.dayOfMonth)
		}.sortedBy { random }.limit(limit)
		if (sequence.isEmpty()) {
			requestAndInsertList(date)
		}
		return sequence.toList()
	}

	fun getContent(history: TodayInHistory): String {
		return history.content ?: kotlin.run {
			val detail = runBlocking {
				todayInHistoryDetail(history.eId)
			}?.firstOrNull() ?: return@run ""

			history.content = spaceRegEx.replace(detail.content, "\n").trim()
			history.flushChanges()
			history.content!!
		}
	}

	private fun requestAndInsertList(date: LocalDate) {
		val list = runBlocking {
			todayInHistory(date)
		} ?: return
		databaseGlobal.bulkInsertOrUpdate(TTodayInHistory) {
			for (history in list) {
				item {
					set(it.month, date.monthValue)
					set(it.day, date.dayOfMonth)
					set(it.date, history.date)
					set(it.title, history.title)
					set(it.eId, history.eId)
				}
			}
			onConflict(it.title) {
				set(it.date, excluded(it.date))
				set(it.eId, excluded(it.eId))
			}
		}
	}

	/**
	 * 历史上的今天
	 */
	private suspend fun todayInHistory(date: LocalDate): List<JuheTodayInHistory>? {
		val todayInHistory = PlugConfig.juheApi["todayInHistory"] ?: return null
		return KtorUtils.get(
			"http://v.juhe.cn/todayOnhistory/queryEvent.php" +
				"?key=${todayInHistory}&date=${date.monthValue}/${date.dayOfMonth}"
		).body<JuheBase<JuheTodayInHistory>>().result
	}

	private suspend fun todayInHistoryDetail(eid: Int): List<JuheTodayInHistoryDetail>? {
		val todayInHistory = PlugConfig.juheApi["todayInHistory"] ?: return null
		val body = KtorUtils.get(
			"http://v.juhe.cn/todayOnhistory/queryDetail.php" +
				"?key=${todayInHistory}&e_id=${eid}"
		).body<JuheBase<JuheTodayInHistoryDetail>>()
		return body.result
	}
}
