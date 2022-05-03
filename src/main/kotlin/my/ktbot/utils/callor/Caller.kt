package my.ktbot.utils.callor

import my.ktbot.annotation.AutoCall
import my.ktbot.annotation.Qualifier
import my.ktbot.interfaces.Plug
import my.ktbot.utils.toMassage
import net.mamoe.mirai.console.util.cast
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.isAccessible

internal class Caller constructor(
	private val obj: Any,
	private val callable: KCallable<*>,
	autoCall: AutoCall,
) : Plug(
	name = autoCall.name,
	regex = Regex(autoCall.regex.pattern, autoCall.regex.option.toSet()),
	weight = autoCall.weight,
	needAdmin = autoCall.needAdmin,
	help = if (autoCall.help.isBlank()) null else PlainText(autoCall.help),
	deleteMSG = autoCall.deleteMSG,
	speedLimit = autoCall.speedLimit,
	expPrivate = autoCall.expPrivate,
	expGroup = autoCall.expGroup,
	msgLength = autoCall.msgLength.start..autoCall.msgLength.endInclusive,
	hidden = autoCall.hidden
) {
	private val tmp = ObjectMap("tmp")

	init {
		callable.isAccessible = true
	}

	private val args = callable.parameters.drop(1).map {
		it.type.classifier.cast<KClass<*>>().java to it.annotations.filterIsInstance<Qualifier>().firstOrNull()?.name
	}

	suspend operator fun invoke(): Any? {
		return callable.callSuspend(obj, *Array(args.size) {
			val (first, name) = args[it]
			tmp[first, name] ?: ObjectMap.global[first, name] ?: return null
		})
	}

	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message? {
		tmp.clear()
		tmp + event + result
		return invoke().toMassage()
	}

}
