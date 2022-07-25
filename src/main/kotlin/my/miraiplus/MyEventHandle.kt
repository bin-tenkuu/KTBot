package my.miraiplus

import my.miraiplus.annotation.MessageHandle
import my.miraiplus.injector.InjectMap
import net.mamoe.mirai.console.plugin.jvm.AbstractJvmPlugin
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.globalEventChannel
import java.lang.reflect.AnnotatedElement
import kotlin.reflect.*
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

class MyEventHandle(
	private val plugin: AbstractJvmPlugin,
) {
	/**
	 * key:[KCallable.toString]
	 */
	private val map = HashMap<String, Listener<*>>()

	@JvmField
	val callers = ArrayList<Caller>()

	@JvmField
	val injector = InjectMap()

	// region register

	operator fun plus(obj: Any): MyEventHandle {
		register(obj)
		return this
	}

	operator fun plusAssign(objs: Array<Any>) {
		for (obj in objs) {
			register(obj)
		}
	}

	fun register(obj: Any) {
		for (member: KCallable<*> in obj::class.declaredMembers) register0(obj, member)
	}

	fun register(obj: Any, member: String) {
		val kCallable = obj::class.declaredMembers.find { it.name == member } ?: return
		register0(obj, kCallable)
	}

	private fun register0(obj: Any, member: KCallable<*>) {
		if (member.toString() in map) {
			return
		}
		val caller: Caller
		val messageHandle: MessageHandle
		when (member) {
			is KFunction<*> -> {
				messageHandle = (member.MessageHandle() ?: return)
				caller = if (member.isSuspend) Caller.Func(obj, member, messageHandle, injector)
				else Caller.JavaFunc(obj, member, messageHandle, injector)
			}
			is KProperty1<*, *> -> {
				val field = member.javaField
				messageHandle =
					member.MessageHandle() ?: member.getter.MessageHandle() ?: field?.MessageHandle() ?: return
				caller = if (field !== null) Caller.JavaField(obj, member, messageHandle, injector)
				else Caller.Property1(obj, member, messageHandle, injector)
			}
			is KProperty2<*, *, *> -> {
				val getter = member.getter
				val field = getter.javaMethod
				messageHandle = member.MessageHandle() ?: getter.MessageHandle() ?: return
				caller = if (field !== null) Caller.JavaFunc(obj, getter, messageHandle, injector)
				else Caller.Property2(obj, member, messageHandle, injector)
			}
			else -> {
				System.err.println(member)
				return
			}
		}
		callers += caller
		caller.init()
		map[caller.fieldName] = plugin.globalEventChannel().subscribeAlways(
			caller.eventClass, plugin.coroutineContext, messageHandle.concurrency, messageHandle.priority, caller
		)
	}

	// endregion

	// region unregister

	fun unregister(caller: Caller) {
		callers.remove(caller)
		map.remove(caller.fieldName)?.complete()
	}

	fun unregisterAll() {
		callers.clear()
		map.values.removeIf { it.complete(); true }
	}

	// endregion

	fun isOpen(caller: Caller): Boolean {
		val listener = map[caller.fieldName] ?: return false
		if (listener.isCompleted) {
			map.remove(caller.fieldName)
			return false
		}
		return true
	}

	private fun KCallable<*>.MessageHandle() = annotations.filterIsInstance<MessageHandle>().firstOrNull()

	private fun AnnotatedElement.MessageHandle() = annotations.filterIsInstance<MessageHandle>().firstOrNull()
}
