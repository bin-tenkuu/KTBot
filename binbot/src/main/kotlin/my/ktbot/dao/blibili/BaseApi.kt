package my.ktbot.dao.blibili


import kotlinx.serialization.Serializable

@Serializable
data class BaseApi<T>(
	/**0：成功,60004：直播间不存在*/
	val code: Int, // 0
	/***/
	val data: T,
	/**默认为ok*/
	val message: String, // ok
	/**默认为ok*/
	val msg: String = message, // ok
	val ttl: Int = 0,// 1
)

