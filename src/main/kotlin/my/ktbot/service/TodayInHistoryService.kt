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
import my.ktbot.utils.Sqlite.set
import my.ktbot.utils.Sqlite.setExcluded
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import org.ktorm.support.sqlite.bulkInsertOrUpdate
import java.time.LocalDate

object TodayInHistoryService {
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
			history.content = detail.content
			history.flushChanges()
			detail.content
		}
	}

	private fun requestAndInsertList(date: LocalDate) {
		val list = runBlocking {
			todayInHistory(date)
		} ?: return
		Sqlite.database.bulkInsertOrUpdate(TTodayInHistory) {
			for (history in list) {
				item {
					it.month.set(date.monthValue)
					it.day.set(date.dayOfMonth)
					it.date.set(history.date)
					it.title.set(history.title)
					it.eId.set(history.eId)
				}
				onConflict(it.title) {
					it.date.setExcluded()
					it.eId.setExcluded()
				}
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
		return KtorUtils.get(
			"http://v.juhe.cn/todayOnhistory/queryEvent.php" +
				"?key=${todayInHistory}&e_id=${eid}"
		).body<JuheBase<JuheTodayInHistoryDetail>>().result
	}
}
