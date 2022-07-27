package my.miraiplus

import my.miraiplus.annotation.MiraiEventHandle
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
	 * key:[KCallable.toString]。value:[Caller]注册的[Listener]
	 */
	private val map = HashMap<String, Listener<*>>()

	/**
	 * 存储全部的 [Caller]
	 */
	@JvmField
	val callers = ArrayList<Caller>()

	/**
	 * 存储全部注解增强实例
	 */
	@JvmField
	val injector = InjectMap()

	// region register
	/**
	 * @see [register]
	 * @param obj [Any] 任意对象
	 */
	operator fun plus(obj: Any): MyEventHandle {
		register(obj)
		return this
	}

	/**
	 * @see [register]
	 * @param objs [Array]<[Any]> 任意对象
	 */
	operator fun plusAssign(objs: Array<Any>) {
		for (obj in objs) {
			register(obj)
		}
	}

	/**
	 * 注册 [obj] 内全部成员
	 * @param obj [Any] 任意对象
	 */
	fun register(obj: Any) {
		for (member: KCallable<*> in obj::class.declaredMembers) register0(obj, member)
	}

	/**
	 * 注册 [obj] 内的指定 [member] 名称的成员
	 * @param obj [Any] 任意对象
	 * @param member [String] 对象内已经定义的成员对象
	 * @throws IllegalStateException [member] 并未在 [obj] 定义的成员中找到
	 */
	@Throws(IllegalStateException::class)
	fun register(obj: Any, member: String) {
		val kCallable = obj::class.declaredMembers.find { it.name == member } ?: error("Member '$member' Not Found In $obj")
		register0(obj, kCallable)
	}

	private fun register0(obj: Any, member: KCallable<*>) {
		if (member.toString() in map) {
			return
		}
		val eventHandle: MiraiEventHandle
		val caller: Caller
		when (member) {
			is KFunction<*> -> {
				eventHandle = (member.MiraiEventHandle() ?: return)
				caller = if (member.isSuspend) Caller.Func(obj, member, eventHandle, injector)
				else Caller.JavaFunc(obj, member, eventHandle, injector)
			}
			is KProperty1<*, *> -> {
				val field = member.javaField
				eventHandle =
					member.MiraiEventHandle() ?: member.getter.MiraiEventHandle() ?: field?.MiraiEventHandle() ?: return
				caller = if (field !== null) Caller.JavaField(obj, member, eventHandle, injector)
				else Caller.Property1(obj, member, eventHandle, injector)
			}
			is KProperty2<*, *, *> -> {
				val getter = member.getter
				val field = getter.javaMethod
				eventHandle = member.MiraiEventHandle() ?: getter.MiraiEventHandle() ?: return
				caller = if (field !== null) Caller.JavaFunc(obj, getter, eventHandle, injector)
				else Caller.Property2(obj, member, eventHandle, injector)
			}
			else -> {
				System.err.println(member)
				return
			}
		}
		callers += caller
		caller.init()
		map[caller.fieldName] = plugin.globalEventChannel().subscribeAlways(
			caller.eventClass, plugin.coroutineContext, eventHandle.concurrency, eventHandle.priority, caller
		)
	}

	// endregion

	// region unregister

	/**
	 * 统一解除全部方法的注册
	 */
	fun unregisterAll() {
		callers.clear()
		val each = map.values.iterator()
		while (each.hasNext()) {
			each.next().complete()
			each.remove()
		}
	}

	// endregion

	private fun KCallable<*>.MiraiEventHandle() = annotations.filterIsInstance<MiraiEventHandle>().firstOrNull()

	private fun AnnotatedElement.MiraiEventHandle() = annotations.filterIsInstance<MiraiEventHandle>().firstOrNull()
}
