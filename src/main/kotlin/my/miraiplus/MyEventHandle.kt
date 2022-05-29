package my.miraiplus

import my.miraiplus.annotation.MessageHandle
import my.miraiplus.injector.InjectMap
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.safeCast
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import java.lang.reflect.AnnotatedElement
import kotlin.reflect.*
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.javaField

class MyEventHandle(
	private val plugin: KotlinPlugin
) {
	private val map = HashMap<String, Listener<*>>()
	val callers = ArrayList<Caller>()

	@JvmField
	val injector = InjectMap()

	operator fun plus(obj: Any): MyEventHandle {
		register(obj)
		return this
	}

	fun register(obj: Any) {
		for (member: KCallable<*> in obj::class.declaredMembers) register0(obj, member)
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
				caller = (if (field !== null) Caller.JavaField(obj, member, messageHandle, injector)
				else Caller.Property1(obj, member, messageHandle, injector))
			}
			is KProperty2<*, *, *> -> {
				messageHandle = member.MessageHandle() ?: member.getter.MessageHandle() ?: return
				caller = Caller.Property2(obj, member, messageHandle, injector)
			}
			else -> {
				System.err.println(member)
				return
			}
		}
		if (messageHandle.name.isEmpty()) {
			callers += caller
		}
		val typeE = member.parameters.mapNotNull {
			it.type.classifier.safeCast<KClass<out MessageEvent>>()
		}.find(MessageEvent::class::isSuperclassOf) ?: MessageEvent::class
		map[member.toString()] = plugin.globalEventChannel().subscribeAlways(
			typeE, plugin.coroutineContext, messageHandle.concurrency, messageHandle.priority, caller
		)
	}

	fun register(obj: Any, member: KCallable<*>) = register(obj, member.name)

	fun register(obj: Any, member: String) {
		val kCallable = obj::class.declaredMembers.find { it.name == member } ?: return
		register0(obj, kCallable)
	}

	fun unregister(member: KCallable<*>) = unregister(member.toString())

	fun unregister(member: String) {
		map.remove(member)?.complete()
	}

	fun unregisterAll() {
		map.values.removeIf { it.complete(); true }
	}

	private fun KCallable<*>.MessageHandle() = annotations.filterIsInstance<MessageHandle>().firstOrNull()

	private fun AnnotatedElement.MessageHandle() = annotations.filterIsInstance<MessageHandle>().firstOrNull()
}
