package my.miraiplus.annotation

import net.mamoe.mirai.event.ConcurrencyKind
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.MessageEvent
import kotlin.reflect.KClass

/**
 * 消息匹配
 * @constructor
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER)
@Retention
@MustBeDocumented
annotation class MiraiEventHandle(
    /**
     * 名称，未指定（空字符串）时使用默认格式。
     */
    val name: String = "",
    /**
     * 默认 [ConcurrencyKind.CONCURRENT]
     * @see [ConcurrencyKind]
     */
    val concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    /**
     * 事件优先级，默认 [EventPriority.NORMAL]
     * @see [EventPriority]
     */
    val priority: EventPriority = EventPriority.NORMAL,
    /**
     * 当且仅当：被注解的方法参数列表中没有明确写出具体的事件类型时，才会使用。
     * 默认为 [MessageEvent]，最大可支持范围为 [Event]
     */
    val eventType: KClass<out Event> = MessageEvent::class,
)
