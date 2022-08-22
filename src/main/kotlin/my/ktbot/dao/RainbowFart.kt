package my.ktbot.dao


import kotlinx.serialization.Serializable

@Serializable
data class RainbowFart(
	var data: Data,
) {
	@Serializable
	data class Data(
		var text: String,
		var type: String,
	)
}
