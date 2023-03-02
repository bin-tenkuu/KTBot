package my.ktbot.utils.mihoyo.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class MihoyoLunaInfo(
	@SerialName("is_sign")
	val isSign: Boolean = false, // true
	@SerialName("is_sub")
	val isSub: Boolean, // true
	@SerialName("region")
	val region: String, // ptr01
	@SerialName("short_sign_day")
	val shortSignDay: Int, // 0
	@SerialName("sign_cnt_missed")
	val signCntMissed: Int, // 1
	@SerialName("today")
	val today: String, // 2023-02-16
	@SerialName("total_sign_day")
	val totalSignDay: Int = 0, // 15
)
