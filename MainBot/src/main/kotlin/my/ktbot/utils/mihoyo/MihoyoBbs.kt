package my.ktbot.utils.mihoyo

import my.ktbot.PlugConfig
import my.ktbot.utils.KtorUtils
import my.ktbot.utils.createLogger
import my.ktbot.utils.mihoyo.Tools.bbs_tasks_list
import my.ktbot.utils.mihoyo.model.MihoyoModel
import my.ktbot.utils.mihoyo.model.TaskList
import kotlin.collections.set

/**
 * @author bin
 * @since 2023/02/15
 */
object MihoyoBbs : suspend () -> Unit {
	private val logger = createLogger<MihoyoBbs>()
	var today_get_coins: Int = 0
	var today_have_get_coins: Int = 0
	var have_coins: Int = 0

	private fun header(): LinkedHashMap<String, String> {
		val map = LinkedHashMap<String, String>()
		map["DS"] = Tools.getDs(false)
		map["cookie"] = "stuid=${PlugConfig.mihoyo.stuid};stoken=${PlugConfig.mihoyo.stoken}"
		map["x-rpc-client_type"] = Tools.mihoyobbs_Client_type
		map["x-rpc-app_version"] = Tools.mihoyobbs_version
		map["x-rpc-sys_version"] = "12"
		map["x-rpc-channel"] = "miyousheluodi"
		map["x-rpc-device_id"] = Tools.get_device_id()
		map["x-rpc-device_name"] = Tools.random_text((1..10).random())
		map["x-rpc-device_model"] = "Mi 10"
		map["Referer"] = "https://app.mihoyo.com"
		map["Host"] = "bbs-api.mihoyo.com"
		map["User-Agent"] = "okhttp/4.8.0"
		return map
	}

	private object TaskDo {
		var bbs_Sign: Boolean = false
		var bbs_Read_posts: Boolean = false
		var bbs_Read_posts_num: Int = 3
		var bbs_Like_posts: Boolean = false
		var bbs_Like_posts_num: Int = 5
		var bbs_Share: Boolean = false
	}

	/**
	 * TODO
	 */
	override suspend fun invoke() {
		get_tasks_list()
		// 如果这三个任务都做了就没必要获取帖子了
		if (TaskDo.bbs_Sign && TaskDo.bbs_Read_posts && TaskDo.bbs_Like_posts && TaskDo.bbs_Share) {
			logger.info("今天已经全部完成了！一共获得 $today_have_get_coins 个米游币，目前有 $have_coins 个米游币")
			return
		}
	}

	private suspend fun get_tasks_list() {
		logger.info("正在获取任务列表")
		val model = KtorUtils.get(bbs_tasks_list) {
			Tools.headers(header())
		}.body<MihoyoModel<TaskList>>()
		if (model.retcode == -100 || "err" in model.message) {
			logger.error("获取任务列表失败，你的cookie可能已过期，请重新设置cookie。")
			PlugConfig.mihoyo.clear_cookies()
			return
		}
		val data = model.data
		today_get_coins = data.canGetPoints
		today_have_get_coins = data.alreadyReceivedPoints
		have_coins = data.totalPoints
		// 如果当日可获取米游币数量为0直接判断全部任务都完成了
		if (today_get_coins == 0) {
			TaskDo.bbs_Sign = true
			TaskDo.bbs_Read_posts = true
			TaskDo.bbs_Like_posts = true
			TaskDo.bbs_Share = true
			return
		}
		// 如果第0个大于或等于62则直接判定任务没做
		if (data.states[0].missionId >= 62) {
			logger.info("新的一天，今天可以获得 $today_get_coins 个米游币")
			return
		}
		logger.info("似乎还有任务没完成，今天还能获得 $today_get_coins")
		for (i in data.states) {
			when (i.missionId) {
				// 58是讨论区签到
				58 -> TaskDo.bbs_Sign = i.isGetAward
				// 59是看帖子
				59 -> {
					TaskDo.bbs_Read_posts = i.isGetAward
					TaskDo.bbs_Read_posts_num = 3 - i.happenedTimes
				}
				// 60是给帖子点赞
				60 -> {
					TaskDo.bbs_Like_posts = i.isGetAward
					TaskDo.bbs_Like_posts_num = 5 - i.happenedTimes
				}
				// 61是分享帖子
				61 -> TaskDo.bbs_Share = i.isGetAward
			}
		}
	}

	private suspend fun get_list() {
		logger.info("正在获取帖子列表......")
		val model = KtorUtils.get(Tools.bbs_post_list_url.format(Tools.mihoyobbs_List[0].forumId)) {
			Tools.headers(header())
		}.body<String>()
		println(model)
	}
}
