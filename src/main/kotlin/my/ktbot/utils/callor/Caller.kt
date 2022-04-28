package my.ktbot.utils.callor

import my.ktbot.annotation.AutoCall
import my.ktbot.interfaces.Plug
import my.ktbot.utils.toMassage
import net.mamoe.mirai.console.util.cast
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend

internal class Caller constructor(
	private val obj: Any,
	private val callable: KCallable<*>,
	private val autoCall: AutoCall,
) : Comparable<Caller> {
	private val args = callable.parameters.drop(1).map {
		it.type.classifier.cast<KClass<*>>().java
	}
	internal val set = args.toSet()

	suspend operator fun invoke(map: ObjectMap): Any? {
		return callable.callSuspend(obj, *Array(args.size) {
			map[args[it]] ?: return null
		})
	}

	override operator fun compareTo(other: Caller): Int {
		return autoCall.weight.compareTo(other.autoCall.weight)
	}

	fun toPlug(map: ObjectMap): Plug {
		val clone = map.clone()
		val plug: Plug = object : Plug(
			name = autoCall.name,
			regex = Regex(autoCall.regexAnn.pattern, autoCall.regexAnn.option.toSet()),
			weight = autoCall.weight,
		) {
			override val deleteMSG = autoCall.deleteMSG
			override val speedLimit = autoCall.speedLimit
			override val help: Message? = null
			override val expPrivate = autoCall.expPrivate
			override val expGroup = autoCall.expGroup
			override val msgLength = autoCall.msgLength.start..autoCall.msgLength.endInclusive
			override val needAdmin = true
			override val hidden = autoCall.hidden
			private val caller: Caller = this@Caller
			override suspend fun invoke(event: MessageEvent, result: MatchResult): Message? {
				TODO("临时加入，后删除")
				clone + event + result
				return caller.invoke(clone).toMassage()
			}
		}
		clone + plug
		return plug
	}
}
