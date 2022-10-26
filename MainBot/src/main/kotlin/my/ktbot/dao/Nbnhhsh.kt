package my.ktbot.dao

import kotlinx.serialization.Serializable

/**
 * @author bin
 * @date 2022/09/14
 */
@Serializable
class Nbnhhsh(
	val name: String,
	val trans: List<String> = emptyList(),
)
