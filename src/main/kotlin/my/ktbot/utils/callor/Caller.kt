package my.ktbot.utils.callor

import my.ktbot.annotation.AutoCall
import net.mamoe.mirai.console.util.cast
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend

internal class Caller constructor(
	private val obj: Any,
	private val callable: KCallable<*>,
	private val autoCall: AutoCall
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
}
