package my.miraiplus

import my.miraiplus.annotation.MessageHandle
import my.miraiplus.annotation.Qualifier
import my.miraiplus.injector.InjectMap
import my.miraiplus.injector.Injector
import net.mamoe.mirai.console.util.cast
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.reflect.*
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

sealed class Caller(
	@JvmField
	val obj: Any,
	callable: KCallable<*>,
	messageHandle: MessageHandle,
	private val injector: InjectMap,
) : suspend (MessageEvent, MessageEvent) -> Unit {
	companion object {
		private val logger = MiraiLogger.Factory.create(Caller::class.java)
	}

	val name = if (messageHandle.name.isEmpty()) messageHandle.name else callable.toString()
	val tmp = ObjectMap("tmp")

	val anns: List<Annotation> = callable.annotations
	val regex = Regex(messageHandle.pattern, messageHandle.options.toSet())


	protected fun Pair<Class<out Any>, String?>.get() = tmp[first, second] ?: ObjectMap.global[first, second]

	protected abstract suspend operator fun invoke(): Any?

	override suspend fun invoke(event: MessageEvent, p2: MessageEvent) {
		val matchResult = regex.find(event.message.contentToString()) ?: return
		tmp + event + matchResult
		val deque = ArrayList<Inject>(anns.size)
		for (ann in anns) {
			val list = injector[ann.annClass] ?: continue
			for (injector in list) {
				deque.add(Inject(ann, injector))
			}
		}
		deque.sort()
		for (inj in deque) inj.doBefore(event)
		val any = invoke()
		while (deque.isNotEmpty()) {
			deque.removeLast().doAfter(event, any)
		}
		tmp.clear()
	}

	@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "UNCHECKED_CAST")
	private val <T : Annotation> T.annClass: Class<T>
		get() = (this as java.lang.annotation.Annotation).annotationType() as Class<T>

	// region impl

	class JavaFunc(
		obj: Any,
		property: KFunction<*>,
		messageHandle: MessageHandle,
		injector: InjectMap,
	) : Caller(obj, property, messageHandle, injector) {
		private val callable = property.javaMethod!!

		init {
			callable.isAccessible = true
		}

		private val args = property.parameters.drop(1).map {
			it.type.classifier.cast<KClass<*>>().java to
				it.annotations.filterIsInstance<Qualifier>().firstOrNull()?.name
		}

		override suspend operator fun invoke(): Any? {
			try {
				return callable.invoke(obj, *Array(args.size) {
					args[it].get() ?: return null
				})
			}
			catch (e: Exception) {
				logger.error(e.cause ?: e)
				return null
			}
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

	// endregion

	private inner class Inject(
		val ann: Annotation,
		val inj: Injector<Annotation>
	) : Comparable<Inject> {
		override fun compareTo(other: Inject): Int = inj.weight.compareTo(other.inj.weight)
		suspend fun doBefore(e: MessageEvent) = inj.doBefore(ann, e, this@Caller)
		suspend fun doAfter(e: MessageEvent, any: Any?) = inj.doAfter(ann, e, this@Caller, any)
	}
}
