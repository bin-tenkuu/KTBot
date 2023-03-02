package my.ktbot.utils.mihoyo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author bin
 * @since 2023/02/16
 */

@Serializable
class LModel<T>(
	@SerialName("list")
	val list: List<T>,
)
