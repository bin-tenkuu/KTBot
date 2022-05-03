package my.ktbot.utils.callor

import my.ktbot.annotation.AutoCall
import my.ktbot.annotation.Qualifier
import my.ktbot.interfaces.Plug
import my.ktbot.utils.toMassage
import net.mamoe.mirai.console.util.cast
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import java.lang.reflect.Field
import kotlin.reflect.*
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.isAccessible

internal sealed class Caller(
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
	protected val tmp = ObjectMap("tmp")

	abstract suspend operator fun invoke(): Any?

	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message? {
		tmp.clear()
		tmp + event + result
		return invoke().toMassage()
	}

	class Func(
		private val obj: Any,
		private val callable: KFunction<*>,
		autoCall: AutoCall,
	) : Caller(autoCall) {
		init {
			callable.isAccessible = true
		}

		private val args = callable.parameters.drop(1).map {
			it.type.classifier.cast<KClass<*>>().java to it.annotations.filterIsInstance<Qualifier>()
				.firstOrNull()?.name
		}

		override suspend operator fun invoke(): Any? {
			return callable.callSuspend(obj, *Array(args.size) {
				tmp[args[it]] ?: ObjectMap.global[args[it]] ?: return null
			})
		}
	}

	class JavaField(
		private val obj: Any,
		private val callable: Field,
		autoCall: AutoCall,
	) : Caller(autoCall) {
		init {
			callable.isAccessible = true
		}

		override suspend operator fun invoke(): Any? {
			return callable.get(obj)
		}
	}

	class Property1(
		private val obj: Any,
		property: KProperty1<*, *>,
		autoCall: AutoCall,
	) : Caller(autoCall) {
		private val callable = property.getter

		init {
			callable.isAccessible = true
		}

		override suspend operator fun invoke(): Any? {
			return callable.call(obj)
		}
	}

	class Property2(
		private val obj: Any,
		property: KProperty2<*, *, *>,
		autoCall: AutoCall,
	) : Caller(autoCall) {
		private val callable = property.getter

		init {
			callable.isAccessible = true
		}

		private val arg = callable.parameters[1].run {
			type.classifier.cast<KClass<*>>().java to annotations.filterIsInstance<Qualifier>().firstOrNull()?.name
		}

		override suspend operator fun invoke(): Any? {
			return callable.call(obj, tmp[arg] ?: ObjectMap.global[arg] ?: return null)
		}
	}
}
