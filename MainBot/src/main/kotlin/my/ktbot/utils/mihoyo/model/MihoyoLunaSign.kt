package my.ktbot.utils.mihoyo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class MihoyoLunaSign(
    @SerialName("challenge")
    val challenge: String,
    @SerialName("code")
    val code: String,
    @SerialName("gt")
    val gt: String,
    @SerialName("is_risk")
    val isRisk: Boolean, // false
    @SerialName("risk_code")
    val riskCode: Int, // 0
    @SerialName("success")
    val success: Int // 0
)
