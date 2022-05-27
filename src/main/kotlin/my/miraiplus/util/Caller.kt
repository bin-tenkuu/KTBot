package my.miraiplus.util

import my.ktbot.annotation.Qualifier
import my.ktbot.utils.callor.ObjectMap
import my.miraiplus.annotation.MessageHandle
import my.miraiplus.injector.InjectMap
import my.miraiplus.injector.Injector
import net.mamoe.mirai.console.util.cast
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.reflect.*
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

abstract class Caller(
	@JvmField
	protected val obj: Any,
	callable: KCallable<*>,
	@JvmField
	protected val messageHandle: MessageHandle,
	@JvmField
	protected val injector: InjectMap,
) : suspend (MessageEvent, MessageEvent) -> Unit {
	companion object {
		private val logger = MiraiLogger.Factory.create(Caller::class.java)
	}

	private val anns: List<Annotation> = callable.annotations
	protected val regex = Regex(messageHandle.pattern, messageHandle.option.toSet())

	private val tmp = ObjectMap("tmp")

	protected fun Pair<Class<out Any>, String?>.get() = tmp[first, second] ?: ObjectMap.global[first, second]

	abstract suspend operator fun invoke(): Any?

	override suspend fun invoke(p1: MessageEvent, p2: MessageEvent) {
		val deque = ArrayList<Inject>(anns.size)
		for (ann in anns) {
			val list = injector[ann.javaClass] ?: continue
			for (injector in list) {
				deque.add(Inject(ann, injector))
			}
		}
		deque.sort()
		for (inj in deque) inj(p1)
		val any = invoke()
		while (deque.isNotEmpty()) {
			deque.removeLast()(p1, any)
		}
	}

	class Func(
		obj: Any,
		private val property: KFunction<*>,
		messageHandle: MessageHandle,
		injector: InjectMap,
	) : Caller(obj, property, messageHandle, injector) {
		init {
			property.isAccessible = true
		}

		private val args = property.parameters.drop(1).map {
			it.type.classifier.cast<KClass<*>>().java to
				it.annotations.filterIsInstance<Qualifier>().firstOrNull()?.name
		}

		override suspend operator fun invoke(): Any? {
			try {
				return property.callSuspend(obj, *Array(args.size) {
					args[it].get() ?: return null
				})
			}
			catch (e: Exception) {
				logger.error(e.cause ?: e)
				return null
			}
		}
	}

	class JavaField(
		obj: Any,
		property: KProperty1<*, *>,
		messageHandle: MessageHandle,
		injector: InjectMap,
	) : Caller(obj, property, messageHandle, injector) {
		private val callable = property.javaField!!

		init {
			callable.isAccessible = true
		}

		override suspend operator fun invoke(): Any? = callable.get(obj)
	}

	class Property1(
		obj: Any,
		property: KProperty1<*, *>,
		messageHandle: MessageHandle,
		injector: InjectMap,
	) : Caller(obj, property, messageHandle, injector) {
		private val callable = property.getter

		init {
			callable.isAccessible = true
		}

		override suspend operator fun invoke(): Any? = callable.call(obj)
	}

	class Property2(
		obj: Any,
		property: KProperty2<*, *, *>,
		messageHandle: MessageHandle,
		injector: InjectMap,
	) : Caller(obj, property, messageHandle, injector) {
		private val callable = property.getter

		init {
			callable.isAccessible = true
		}

		private val arg = callable.parameters[1].run {
			type.classifier.cast<KClass<*>>().java to annotations.filterIsInstance<Qualifier>().firstOrNull()?.name
		}

		override suspend operator fun invoke(): Any? {
			return callable.call(obj, arg.get() ?: return null)
		}
	}

	private inner class Inject(val ann: Annotation, val inj: Injector<Annotation>) : Comparable<Inject> {
		override fun compareTo(other: Inject): Int = inj.weight compareTo other.inj.weight
		operator fun invoke(e: MessageEvent) = inj.doBefore(ann, e)
		operator fun invoke(e: MessageEvent, any: Any?) = inj.doAfter(ann, e, any)
	}
}
