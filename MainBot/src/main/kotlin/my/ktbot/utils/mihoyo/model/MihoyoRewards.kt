package my.ktbot.utils.mihoyo.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class MihoyoRewards(
	@SerialName("awards")
	val awards: List<Award>,
	@SerialName("biz")
	val biz: String, // bh2
	@SerialName("month")
	val month: Int, // 2
	@SerialName("resign")
	val resign: Boolean, // false
	@SerialName("short_extra_award")
	val shortExtraAward: ShortExtraAward,
) {
	@Serializable
	class Award(
		@SerialName("cnt")
		val cnt: Int, // 10
		@SerialName("icon")
		val icon: String, // https://webstatic.mihoyo.com/upload/event/2022/03/24/f75b71329ede10e62b2d5372c5a34422_8826342261704907873.png
		@SerialName("name")
		val name: String, // 吃货啾啾
	)

	@Serializable
	class ShortExtraAward(
		@SerialName("end_time")
		val endTime: String,
		@SerialName("end_timestamp")
		val endTimestamp: String, // 0
		@SerialName("has_extra_award")
		val hasExtraAward: Boolean, // false
		@SerialName("start_time")
		val startTime: String,
		@SerialName("start_timestamp")
		val startTimestamp: String, // 0
	)
}
