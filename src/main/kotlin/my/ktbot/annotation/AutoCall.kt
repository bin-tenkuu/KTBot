package my.ktbot.annotation

import my.miraiplus.annotation.RegexAnn
import kotlin.annotation.AnnotationTarget.*

@Target(FUNCTION, FIELD, PROPERTY, PROPERTY_GETTER)
@Retention
@MustBeDocumented
annotation class AutoCall(
	/**名称*/
	val name: String,
	/**正则匹配*/
	val regex: RegexAnn,
	/**权重*/
	val weight: Double,
	/**是否需要管理员*/
	val needAdmin: Boolean = false,
	/**帮助文本*/
	val help: String = "",
	/**存在时延时固定时间撤回，单位ms*/
	val deleteMSG: Long = 0,
	/**存在时启用调用限速，单位ms*/
	val speedLimit: Long = 0,
	/**私聊经验*/
	val expPrivate: Double = 0.0,
	/**群聊经验*/
	val expGroup: Double = 0.0,
	/**调用消息长度*/
	val msgLength: MsgLength = MsgLength(0, 100),
	/**隐藏*/
	val hidden: Boolean = false,
)

