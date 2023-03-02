package my.ktbot.utils.mihoyo.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class MihoyoModel<T>(
	@SerialName("data")
	val `data`: T,
	@SerialName("message")
	val message: String, // OK
	@SerialName("retcode")
	val retcode: Int, // 0
)

