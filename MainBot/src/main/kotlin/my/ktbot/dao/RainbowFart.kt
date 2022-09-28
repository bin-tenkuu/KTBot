package my.ktbot.dao


import kotlinx.serialization.Serializable

@Serializable
data class RainbowFart(
	val data: Data,
) {
	@Serializable
	data class Data(
		val text: String,
		val type: String,
	)
}
