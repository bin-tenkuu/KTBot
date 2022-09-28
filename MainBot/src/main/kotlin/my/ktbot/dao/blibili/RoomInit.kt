package my.ktbot.dao.blibili

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomInit(
	/**是否加密*/
	val encrypted: Boolean, // false
	/**隐藏时间戳*/
	@SerialName("hidden_till")
	val hiddenTill: Long, // 0
	/**是否隐藏	*/
	@SerialName("is_hidden")
	val isHidden: Boolean, // false
	/**是否锁定*/
	@SerialName("is_locked")
	val isLocked: Boolean, // false
	/**是否竖屏*/
	@SerialName("is_portrait")
	val isPortrait: Boolean, // false
	/**是否为特殊直播间,0：普通直播间,1：付费直播间*/
	@SerialName("is_sp")
	val isSp: Int, // 0
	/**直播状态0：未开播,1：直播中,2：轮播中*/
	@SerialName("live_status")
	val liveStatus: Int, // 1
	/**开播时间,未开播时为-62170012800*/
	@SerialName("live_time")
	val liveTime: Long, // 1643425514
	/**锁定时间戳*/
	@SerialName("lock_till")
	val lockTill: Long, // 0
	/**是否p2p*/
	@SerialName("need_p2p")
	val needP2p: Int, // 0
	/**加密房间是否通过密码验证*/
	@SerialName("pwd_verified")
	val pwdVerified: Boolean, // false
	/**直播间真实id*/
	@SerialName("room_id")
	val roomId: Int, // 5279
	/***/
	@SerialName("room_shield")
	val roomShield: Int, // 1
	/**直播间id（短号）*/
	@SerialName("short_id")
	val shortId: Int, // 102
	/**特殊直播间标志,0：普通直播间,1：付费直播间,2：拜年祭直播间*/
	@SerialName("special_type")
	val specialType: Int, // 0
	/**主播用户mid*/
	val uid: Int, // 110631
)