package my.miraiplus

import my.miraiplus.annotation.MiraiEventHandle
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.globalEventChannel
import java.lang.reflect.AnnotatedElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.*
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

open class MyKotlinPlugin(
	description: JvmPluginDescription,
	parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : JvmPlugin, KotlinPlugin(description, parentCoroutineContext) {
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
	val injectMap = InjectMap()

	// region register
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
		val kCallable = obj::class.declaredMembers.find { it.name == member } ?: error(
			"Member '$member' Not Found In $obj"
		)
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
				caller = if (member.isSuspend) Caller.Func(obj, member, eventHandle, injectMap)
				else Caller.JavaFunc(obj, member, eventHandle, injectMap)
			}
			is KProperty1<*, *> -> {
				val field = member.javaField
				eventHandle =
					member.MiraiEventHandle() ?: member.getter.MiraiEventHandle() ?: field?.MiraiEventHandle() ?: return
				caller = if (field !== null) Caller.JavaField(obj, member, eventHandle, injectMap)
				else Caller.Property1(obj, member, eventHandle, injectMap)
			}
			is KProperty2<*, *, *> -> {
				val getter = member.getter
				val field = getter.javaMethod
				eventHandle = member.MiraiEventHandle() ?: getter.MiraiEventHandle() ?: return
				caller = if (field !== null) Caller.JavaFunc(obj, getter, eventHandle, injectMap)
				else Caller.Property2(obj, member, eventHandle, injectMap)
			}
			else -> {
				System.err.println(member)
				return
			}
		}
		callers += caller
		caller.init()
		map[caller.fieldName] = globalEventChannel().subscribeAlways(
			caller.eventClass, coroutineContext, eventHandle.concurrency, eventHandle.priority, caller
		)
	}

	// endregion

	// region unregister

	/**
	 * 统一解除全部方法的注册
	 */
	fun unregisterAll() {
		callers.iterator().let {
			while (it.hasNext()) {
				it.next().destroy()
				it.remove()
			}
		}
		map.values.iterator().let {
			while (it.hasNext()) {
				it.next().complete()
				it.remove()
			}
		}
	}

	// endregion

	private fun KCallable<*>.MiraiEventHandle() = annotations.filterIsInstance<MiraiEventHandle>().firstOrNull()

	private fun AnnotatedElement.MiraiEventHandle() = annotations.filterIsInstance<MiraiEventHandle>().firstOrNull()
}
