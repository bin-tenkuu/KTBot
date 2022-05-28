package my.miraiplus.injector

import my.miraiplus.util.Caller
import net.mamoe.mirai.event.events.MessageEvent

/**
 *
 * @param T : Annotation 注解处理类
 */
abstract class Injector<T : Annotation>(
	/**
	 * 权重：越小，排名越前
	 */
	open val weight: Double = 0.0
) {

	/**
	 * 在方法执行之前执行
	 * @param ann T 注解实例
	 * @param event Event 事件实例
	 * @param caller Caller
	 * @return Boolean true-继续执行，false-取消后续执行
	 */
	open suspend fun doBefore(ann: T, event: MessageEvent, caller: Caller): Boolean = true

	/**
	 * 在方法执行之后执行
	 * @param ann T 注解实例
	 * @param event Event 事件实例
	 * @param caller Caller
	 */
	open suspend fun doAfter(ann: T, event: MessageEvent, caller: Caller, result: Any?) {}
}
