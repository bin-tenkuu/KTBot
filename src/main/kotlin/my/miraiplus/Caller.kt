package my.miraiplus

import my.miraiplus.annotation.MessageHandle
import my.miraiplus.annotation.Qualifier
import my.miraiplus.injector.InjectMap
import my.miraiplus.injector.Injector
import net.mamoe.mirai.console.util.cast
import net.mamoe.mirai.console.util.safeCast
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.reflect.*
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

sealed class Caller(
	@JvmField
	val obj: Any,
	callable: KCallable<*>,
	messageHandle: MessageHandle,
	injector: InjectMap,
) : suspend (Event, Event) -> Unit {
	companion object {
		private val logger = MiraiLogger.Factory.create(Caller::class.java)
	}

	@JvmField
	val fieldName = callable.toString()

	@JvmField
	val name = messageHandle.name.ifEmpty { fieldName }

	@JvmField
	val tmp = ObjectMap("tmp")

	@JvmField
	val eventClass = callable.parameters.mapNotNull {
		it.type.classifier.safeCast<KClass<Event>>()
	}.find(Event::class::isSuperclassOf) ?: messageHandle.eventType

	@JvmField
	val anns: List<Annotation> = callable.annotations

	private val injects = ArrayList<Inject>()

	init {
		for (ann in callable.annotations) {
			val injectors = injector[ann.annClass, eventClass] ?: continue
			@Suppress("UNCHECKED_CAST")
			injectors.mapTo(injects) {
				Inject(ann, it as Injector<Annotation, Event>)
			}
		}
		injects.sort()
	}

	internal fun init() {
		for (injector in injects) injector.init()
	}

	protected fun Pair<Class<out Any>, String?>.get() = tmp[first, second] ?: ObjectMap.global[first, second]

	protected abstract suspend operator fun invoke(): Any?

	override suspend fun invoke(event: Event, p2: Event) {
		val name1 = name
		tmp + event
		for (inj in injects) {
			if (inj.doBefore(event)) continue
			return
		}
		val any: Any? = try {
			invoke()
		}
		catch (e: Exception) {
			e.printStackTrace()
			null
		}

		val iterator = injects.listIterator(injects.size)
		while (iterator.hasPrevious()) {
			iterator.previous().doAfter(event, any)
		}
		tmp.clear()
		name1.length
	}

	@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "UNCHECKED_CAST")
	private val <T : Annotation> T.annClass: Class<T>
		get() = (this as java.lang.annotation.Annotation).annotationType() as Class<T>

	override fun toString(): String = fieldName

	override fun hashCode(): Int = fieldName.hashCode()

	override fun equals(other: Any?): Boolean = when {
		this === other -> true
		other !is Caller -> false
		fieldName == other.fieldName -> true
		else -> false
	}

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
		val inj: Injector<Annotation, Event>
	) : Comparable<Inject> {
		override fun compareTo(other: Inject): Int = inj.weight.compareTo(other.inj.weight)
		fun init() = inj.init(ann, this@Caller)
		suspend fun doBefore(e: Event) = inj.doBefore(ann, e, this@Caller)
		suspend fun doAfter(e: Event, any: Any?) = inj.doAfter(ann, e, this@Caller, any)
	}
}
