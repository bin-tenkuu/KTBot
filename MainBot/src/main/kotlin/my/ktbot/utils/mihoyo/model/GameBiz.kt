package my.ktbot.utils.mihoyo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GameBiz(
	@SerialName("game_biz")
	val gameBiz: String, // bh2_cn
	@SerialName("game_uid")
	val gameUid: String, // 50079873
	@SerialName("is_chosen")
	val isChosen: Boolean, // false
	@SerialName("is_official")
	val isOfficial: Boolean, // false
	@SerialName("level")
	val level: Int, // 333
	@SerialName("nickname")
	val nickname: String, // Kiаna
	@SerialName("region")
	val region: String, // ptr01
	@SerialName("region_name")
	val regionName: String, // 翁德兰
)
