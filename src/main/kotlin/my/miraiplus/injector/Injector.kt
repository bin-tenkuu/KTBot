package my.miraiplus.injector

import my.miraiplus.Caller
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.MessageEvent
import kotlin.reflect.KClass

/**
 *
 * @param T : Annotation 注解处理类
 */
interface Injector<T : Annotation, E : Event> {
	/**
	 * 权重：越小，排名越前
	 */
	val weight: Double get() = 0.0
	val event: KClass<out E>

	/**
	 * 初始化方法，将会在具体运行方法实例化完成之后调用，运行顺序为[weight]顺序
	 * @param ann T
	 * @param caller Caller
	 */
	fun init(ann: T, caller: Caller) {}

	/**
	 * 在方法执行之前执行，运行顺序为[weight]顺序
	 * @param ann T 注解实例
	 * @param event Event 事件实例
	 * @param caller Caller
	 * @return Boolean true-继续执行，false-取消后续执行
	 */
	suspend fun doBefore(ann: T, event: E, caller: Caller): Boolean = true

	/**
	 * 在方法执行之后执行，运行顺序为[weight]倒序
	 * @param ann T 注解实例
	 * @param event Event 事件实例
	 * @param caller Caller
	 */
	suspend fun doAfter(ann: T, event: E, caller: Caller, result: Any?) {}

	interface Message<T : Annotation> : Injector<T, MessageEvent> {
		override val event: KClass<out MessageEvent>
			get() = MessageEvent::class
	}
}
