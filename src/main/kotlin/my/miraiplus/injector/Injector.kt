package my.miraiplus.injector

import net.mamoe.mirai.event.events.MessageEvent

/**
 *
 * @param T : Annotation
 */
abstract class Injector<T : Annotation> {
	open val weight: Double get() = 0.0

	/**
	 * 在方法执行之前执行
	 * @param ann T 注解实例
	 * @param event Event 事件实例
	 * @return Boolean true-取消后续执行，false-继续执行
	 */
	open fun doBefore(ann: T, event: MessageEvent): Boolean = false

	/**
	 * 在方法执行之后执行
	 * @param ann T 注解实例
	 * @param event Event 事件实例
	 * @return Unit
	 */
	open fun doAfter(ann: T, event: MessageEvent, result: Any?) = Unit
}
