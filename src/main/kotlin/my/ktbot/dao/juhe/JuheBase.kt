package my.ktbot.dao.juhe

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JuheBase<T : JuheBase.Base>(
	/**提示信息*/
	val reason: String,
	/**返回码*/
	@SerialName("error_code")
	val errorCode: Int,
	/**实体内容*/
	val result: List<T>?,
) {
	interface Base
}

@Serializable
data class JuheTodayInHistory(
	/**日期*/
	val day: String,
	/**事件日期*/
	val date: String,
	/**事件标题*/
	val title: String,
	/**事件id,即下一接口中所用的e_id*/
	@SerialName("e_id")
	val eId: Int,
) : JuheBase.Base

@Serializable
data class JuheTodayInHistoryDetail(
	/**事件id*/
	@SerialName("e_id")
	val eId: Int,
	/**事件标题*/
	val title: String,
	/**事件详情*/
	val content: String,
) : JuheBase.Base
