package my.ktbot.dao

import kotlinx.serialization.Serializable

/**
 * @author bin
 * @date 2022/09/28
 */
@Serializable
class EveryDay60s(
	val success: Boolean = false,
	val name: String = "",
	val time: Array<String> = emptyArray(),
	val data: Array<String> = emptyArray(),
)
