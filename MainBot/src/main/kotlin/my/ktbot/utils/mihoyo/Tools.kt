package my.ktbot.utils.mihoyo

import io.ktor.client.request.*
import io.ktor.http.*
import my.ktbot.PlugConfig
import my.ktbot.utils.KtorUtils
import my.ktbot.utils.UUID3
import my.ktbot.utils.createLogger
import my.ktbot.utils.mihoyo.model.*
import java.security.MessageDigest

/**
 * @author bin
 * @since 2023/02/16
 */
object Tools {
	private val logger = createLogger<MihoyoBbs>()

	class MihoyoBbsList(
		val id: Int,
		val forumId: Int,
		val name: String,
		val url: String,
	)

	/**
	 * 米游社的Salt
	 */
	const val mihoyobbs_salt = "TsmyHpZg8gFAVKTtlPaL6YwMldzxZJxQ"
	const val mihoyobbs_salt_x4 = "xV8v4Qu54lUKrEYFZkJhB8cuOh9Asafs"
	const val mihoyobbs_salt_x6 = "t0qEgfub6cvueAPgR5m9aQWWVciEer7v"
	const val mihoyobbs_salt_web = "osgT0DljLarYxgebPPHJFjdaxPfoiHGt"

	/**
	 * 米游社的版本,Salt和Version相互对应
	 */
	const val mihoyobbs_version = "2.41.2"

	/**
	 * 米游社的客户端类型,1为ios 2为安卓
	 */
	const val mihoyobbs_Client_type = "2"

	/**
	 * 4为pc web 5为mobile web
	 */
	const val mihoyobbs_Client_type_web = "5"

	/**
	 * 米游社的分区列表
	 */
	val mihoyobbs_List = listOf(
		// MihoyoBbsList(1, 1, "崩坏3", "https://bbs.mihoyo.com/bh3/"),
		// MihoyoBbsList(2, 26, "原神", "https://bbs.mihoyo.com/ys/"),
		MihoyoBbsList(3, 30, "崩坏2", "https://bbs.mihoyo.com/bh2/"),
		// MihoyoBbsList(4, 37, "未定事件簿", "https://bbs.mihoyo.com/wd/"),
		MihoyoBbsList(5, 34, "大别野", "https://bbs.mihoyo.com/dby/"),
		// MihoyoBbsList(6, 52, "崩坏：星穹铁道", "https://bbs.mihoyo.com/sr/"),
		// MihoyoBbsList(8, 57, "绝区零", "https://bbs.mihoyo.com/zzz/"),
	)
	val game_id2name = mapOf(
		"bh2_cn" to "崩坏2",
		"bh3_cn" to "崩坏3",
		"nxx_cn" to "未定事件簿",
		"hk4e_cn" to "原神",
	)
	val game_id2config = mapOf(
		"bh2_cn" to "hokai2",
		"bh3_cn" to "honkai3rd",
		"nxx_cn" to "tears_of_themis",
		"hk4e_cn" to "genshin",
	)

	/**
	 * 游戏签到的请求头
	 */
	val headers: LinkedHashMap<String, String>
		get() = mapOf(
			HttpHeaders.Accept to "application/json, text/plain, */*",
			"DS" to "",
			"x-rpc-channel" to "miyousheluodi",
			HttpHeaders.Origin to "https://webstatic.mihoyo.com",
			HttpHeaders.UserAgent to "Mozilla/5.0 (Linux; U; Android 11; zh-CN; MZ-MEIZU 18s Pro Build/MRA58K) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/73.0.3683.121 MZBrowser/9.15.2 Mobile Safari/537.36 miHoYoBBS/$mihoyobbs_version",
			"x-rpc-client_type" to mihoyobbs_Client_type_web,
			HttpHeaders.Referrer to "",
			HttpHeaders.AcceptEncoding to "gzip, deflate",
			HttpHeaders.AcceptLanguage to "zh-CN,en-US;q=0.8",
			"X-Requested-With" to "com.mihoyo.hyperion",
			HttpHeaders.Cookie to "",
			"x-rpc-device_id" to get_device_id(),
		).toMap(LinkedHashMap())

	context (HttpRequestBuilder)
	fun headers(map: Map<String, String>) {
		for ((key, value) in map) {
			header(key, value)
		}
	}

	/**
	 * 通用设置
	 */
	const val bbs_api = "https://bbs-api.mihoyo.com"
	const val web_api = "https://api-takumi.mihoyo.com"
	const val account_Info_url = "$web_api/binding/api/getUserGameRolesByCookie?game_biz="

	/**
	 * 米游社的API列表
	 */
	const val bbs_cookie_url = "https://webapi.account.mihoyo.com/Api/cookie_accountinfo_by_loginticket?login_ticket={}"
	const val bbs_cookie_url2 = "$web_api/auth/api/getMultiTokenByLoginTicket?login_ticket={}&token_types=3&uid={}"
	const val bbs_tasks_list = "$bbs_api/apihub/sapi/getUserMissionsState" // 获取任务列表
	const val bbs_sign_url = "$bbs_api/apihub/app/api/signIn"  // post
	const val bbs_post_list_url = "$bbs_api/post/api/getForumPostList?forum_id=%s&is_good=false&is_hot=false&page_size=20&sort_type=1"
	const val bbs_detail_url = "$bbs_api/post/api/getPostFull?post_id={}"
	const val bbs_share_url = "$bbs_api/apihub/api/getShareConf?entity_id={}&entity_type=1"
	const val bbs_like_url = "$bbs_api/apihub/sapi/upvotePost"  // post json
	const val bbs_get_captcha = "$bbs_api/misc/api/createVerification?is_high=true"
	const val bbs_captcha_verify = "$bbs_api/misc/api/verifyVerification"

	/**
	 * 崩坏2自动签到相关的相关设置
	 */
	const val honkai2_Act_id = "e202203291431091"
	const val honkai2_checkin_rewards = "$web_api/event/luna/home?lang=zh-cn&act_id=$honkai2_Act_id"
	const val honkai2_Is_signurl = "$web_api/event/luna/info?lang=zh-cn&act_id=$honkai2_Act_id&region=%s&uid=%s"
	const val honkai2_Sign_url = "$web_api/event/luna/sign"

	private const val randomString = "abcdefghijklmnopqrstuvwxyz0123456789"
	fun String.md5(): String {
		return MessageDigest.getInstance("MD5").digest(toByteArray()).joinToString("") { "%02x".format(it) }
	}

	fun random_text(num: Int): String = String(CharArray(num) { randomString.random() })

	fun timestamp() = System.currentTimeMillis() / 1000
	fun getDs(web: Boolean): String {
		val n = if (web) mihoyobbs_salt_web else mihoyobbs_salt
		val i = timestamp()
		val r = random_text(6)
		val c = "salt=$n&t=$i&r=$r".md5()
		return "$i,$r,$c"
	}

	/**
	 * 获取请求Header里的DS(版本2) 这个版本ds之前见到都是查询接口里的
	 * @param q String
	 * @param b String
	 * @return String
	 */
	fun getDs2(q: String, b: String): String {
		val n = mihoyobbs_salt_x6
		val i = timestamp()
		val r = (100001..200000).random()
		val add = "&b=$b&q=$q"
		val c = "salt=$n&t=$i&r=$r$add".md5()
		return "$i,$r,$c"
	}

	fun get_device_id(): String = UUID3.URL.uuid3(PlugConfig.mihoyo.cookie).toString()
	fun get_item(award: MihoyoRewards.Award): String {
		return "${award.name} × ${award.cnt}"
	}

	suspend fun get_account_list(game_id: String, headers: MutableMap<String, String>): List<GameBiz> {
		val gameName = game_id2name[game_id]
		logger.info("正在获取米哈游账号绑定的 $gameName 账号列表...")
		val data = KtorUtils.get(account_Info_url + game_id) {
			headers(headers)
		}.body<MihoyoModel<LModel<GameBiz>>>()
		if (data.retcode != 0) {
			logger.warning("获取 $gameName 账号列表失败！")
			PlugConfig.mihoyo.clear_cookie_game(game_id)
			return emptyList()
		}
		val list = data.data.list
		logger.info("已获取到 ${list.size} 个 $gameName 账号信息")
		return list
	}
}
