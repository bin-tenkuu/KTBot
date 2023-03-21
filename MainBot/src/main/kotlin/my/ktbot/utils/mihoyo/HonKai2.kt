package my.ktbot.utils.mihoyo

import io.ktor.client.request.*
import kotlinx.coroutines.delay
import my.ktbot.PlugConfig
import my.ktbot.utils.KtorUtils
import my.ktbot.utils.createLogger
import my.ktbot.utils.mihoyo.model.*
import kotlin.collections.set

/**
 * 代码来源 [honkai2.py](https://github.com/Womsxd/MihoyoBBSTools/blob/master/honkai2.py)
 * @author bin
 * @since 2023/02/16
 */
object HonKai2 : suspend () -> Unit {
	private val logger = createLogger<HonKai2>()
	private val account_list = ArrayList<GameBiz>()
	private var checkin_rewards: List<MihoyoRewards.Award> = emptyList()

	private fun header(): LinkedHashMap<String, String> {
		val map = Tools.headers
		map["DS"] = Tools.getDs(true)
		map["Referer"] = "https://webstatic.mihoyo.com/bbs/event/signin/bh2/index.html?" +
				"bbs_auth_required=true&act_id=${Tools.honkai2_Act_id}&bbs_presentation_style=fullscreen" +
				"&utm_source=bbs&utm_medium=mys&utm_campaign=icon"
		map["Cookie"] = PlugConfig.mihoyo.cookie
		map["x-rpc-device_id"] = Tools.get_device_id()
		return map
	}

	override suspend fun invoke() {
		logger.info("正在进行崩坏2签到")
		getAccountList()
		if (account_list.isNotEmpty()) {
			checkin_rewards = getCheckinRewards()
		}
		signAccount()
	}

	private suspend fun getAccountList() {
		val list = Tools.get_account_list("bh2_cn", header())
		account_list.addAll(list.filter {
			it.gameUid == "50079873"
		})
	}

	private suspend fun getCheckinRewards(): List<MihoyoRewards.Award> {
		logger.info("正在获取签到奖励列表...")
		val model = KtorUtils.get(Tools.honkai2_checkin_rewards) {
			Tools.headers(header())
		}.body<MihoyoModel<MihoyoRewards>>()
		return model.data.awards
	}

	private suspend fun signAccount() {
		if (account_list.isEmpty()) {
			logger.warning("账号没有绑定任何崩坏学园2账号！")
			return
		}
		for (gameBiz in account_list) {
			logger.info("正在为崩坏2玩家: '${gameBiz.nickname}' 进行签到...")
			delay((5L..15L).random())
			val isSign = isSign(gameBiz.region, gameBiz.gameUid) ?: continue
			val signDay = isSign.totalSignDay
			if (isSign.isSign) {
				val award = checkin_rewards[signDay - 1]
				logger.info("崩坏2玩家: '${gameBiz.nickname}' 今天已经签到过了~")
				logger.info("今天获得的奖励是 ${Tools.get_item(award)}")
			} else {
				delay((5L..15L).random())
				val body = MihoyoLunaSignBody(Tools.honkai2_Act_id, gameBiz.region, gameBiz.gameUid)
				val model = KtorUtils.post(Tools.honkai2_Sign_url) {
					setBody(body)
					Tools.headers(header())
				}.body<MihoyoModel<MihoyoLunaSign>>()
				when (model.retcode) {
					0 -> {
						val award = checkin_rewards[signDay]
						logger.info("崩坏2玩家: '${gameBiz.nickname}' 签到成功~")
						logger.info("今天获得的奖励是 ${Tools.get_item(award)}")
					}
					-5003 -> {
						val award = checkin_rewards[signDay - 1]
						logger.info("崩坏2玩家: '${gameBiz.nickname}' 今天已经签到过了~")
						logger.info("今天获得的奖励是 ${Tools.get_item(award)}")
					}
					else -> logger.warning("账号签到失败！: " + model.message)
				}
			}
		}
	}

	private suspend fun isSign(region: String, uid: String): MihoyoLunaInfo? {
		val model = KtorUtils.get(Tools.honkai2_Is_signurl.format(region, uid)) {
			Tools.headers(header())
		}.body<MihoyoModel<MihoyoLunaInfo>>()
		if (model.retcode != 0) {
			logger.warning("获取账号签到信息失败: " + model.message)
			PlugConfig.mihoyo.games["hokai2"] = false
			return null
		}
		return model.data
	}
}
