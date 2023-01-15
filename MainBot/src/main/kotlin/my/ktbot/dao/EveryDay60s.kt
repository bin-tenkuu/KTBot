package my.ktbot.dao

import kotlinx.serialization.Serializable

/**
 * @author bin
 * @date 2022/09/28
 */
@Serializable
class ZhiHu(
	val data: List<Content> = emptyList(),
	// val paging: Array<String> = emptyArray(),
) {
	@Serializable
	class Content(val content: String)
}

class EveryDay60s(
	val img: String,
	val texts: List<String>,
)
