package my.ktbot.utils.callor

import my.ktbot.annotation.AutoCall
import my.ktbot.annotation.Qualifier
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
	autoCall: AutoCall,
) : Plug(
	name = autoCall.name,
	regex = Regex(autoCall.regexAnn.pattern, autoCall.regexAnn.option.toSet()),
	weight = autoCall.weight,
) {
	override val needAdmin = autoCall.needAdmin
	override val help: Message? = null
	override val deleteMSG = autoCall.deleteMSG
	override val speedLimit = autoCall.speedLimit
	override val expPrivate = autoCall.expPrivate
	override val expGroup = autoCall.expGroup
	override val msgLength = autoCall.msgLength.start..autoCall.msgLength.endInclusive
	override val hidden = autoCall.hidden

	private val args = callable.parameters.drop(1).map {
		it.type.classifier.cast<KClass<*>>().java to it.annotations.filterIsInstance<Qualifier>().firstOrNull()?.name
	}

	suspend operator fun invoke(): Any? {
		return callable.callSuspend(obj, *Array(args.size) {
			val (first, name) = args[it]
			ObjectMap.tmp[first, name] ?: ObjectMap.global[first, name] ?: return null
		})
	}

	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message? {
		ObjectMap.tmp.clear()
		ObjectMap.tmp + event + result
		return invoke().toMassage()
	}

}
