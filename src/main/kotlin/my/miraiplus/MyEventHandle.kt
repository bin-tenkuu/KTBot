package my.miraiplus

import my.miraiplus.annotation.MessageHandle
import my.miraiplus.injector.InjectMap
import my.miraiplus.util.Caller
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.cast
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
	private val map = HashMap<KCallable<*>, Listener<*>>()

	@JvmField
	val injector = InjectMap()

	fun register(obj: Any) {
		for (member: KCallable<*> in obj::class.declaredMembers) {
			register(obj, member)
		}
	}

	fun register(obj: Any, member: KCallable<*>) {
		val caller: Caller
		when (member) {
			is KFunction<*> -> {
				val messageHandle = member.MessageHandle() ?: return
				caller = Caller.Func(obj, member, messageHandle, injector)
			}
			is KProperty1<*, *> -> {
				val field = member.javaField
				val messageHandle =
					member.MessageHandle() ?: member.getter.MessageHandle() ?: field?.MessageHandle() ?: return
				caller = (if (field !== null) Caller.JavaField(obj, member, messageHandle, injector)
				else Caller.Property1(obj, member, messageHandle, injector))
			}
			is KProperty2<*, *, *> -> {
				val messageHandle = member.MessageHandle() ?: member.getter.MessageHandle() ?: return
				caller = Caller.Property2(obj, member, messageHandle, injector)
			}
			else -> {
				System.err.println(member)
				return
			}
		}
		@Suppress("UNCHECKED_CAST")
		val typeE = member.parameters.map {
			it.type.classifier.cast<KClass<out MessageEvent>>()
		}.find(MessageEvent::class::isSuperclassOf) ?: MessageEvent::class
		map[member] = plugin.globalEventChannel().subscribeAlways(typeE, handler = caller)
	}

	fun unRegister(member: KCallable<*>) {
		map.remove(member)?.complete()
	}

	private fun KCallable<*>.MessageHandle() = annotations.filterIsInstance<MessageHandle>().firstOrNull()

	private fun AnnotatedElement.MessageHandle() = annotations.filterIsInstance<MessageHandle>().firstOrNull()

}
