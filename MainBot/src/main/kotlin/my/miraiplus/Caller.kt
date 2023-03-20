package my.miraiplus

import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.Qualifier
import net.mamoe.mirai.console.util.cast
import net.mamoe.mirai.console.util.safeCast
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.utils.MiraiLogger
import java.util.*
import kotlin.collections.ArrayDeque
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
    eventHandle: MiraiEventHandle,
    injector: InjectMap,
) : suspend (Event, Event) -> Unit {
    companion object {
        private val logger = MiraiLogger.Factory.create(Caller::class.java)
    }

    @JvmField
    val fieldName = callable.toString()

    @JvmField
    val name = eventHandle.name.ifEmpty { fieldName }

    @JvmField
    val eventClass = callable.parameters.mapNotNull {
        it.type.classifier.safeCast<KClass<out Event>>()
    }.find { Event::class.isSuperclassOf(it) } ?: eventHandle.eventType

    @JvmField
    val anns: List<Annotation> = callable.annotations

    private val injects = ArrayList<Inject>()

    init {
        for (ann in callable.annotations) {
            val injectors = injector[ann.annClass] ?: continue
            @Suppress("UNCHECKED_CAST")
            injectors.mapTo(injects) { Inject(ann, it as Injector<Annotation, Event>) }
        }
        injects.sort()
    }

    internal fun init() {
        for (injector in injects) injector.init()
    }

    internal fun destroy() {
        for (injector in injects) injector.destory()
    }

    protected operator fun ArgsMap.get(pair: Pair<KType, String?>): Any? {
        val (type, name) = pair
        val clazz = type.classifier.cast<KClass<*>>().java
        return this[clazz, name] ?: ArgsMap.global[clazz, name]
    }

    protected val List<Annotation>.qualifierName get() = filterIsInstance<Qualifier>().firstOrNull()?.name

    protected abstract suspend operator fun invoke(tmp: ArgsMap): Any?

    suspend operator fun invoke(id: String, msg: String): Any? {
        if (eventClass != NeverEvent::class) {
            return null
        }
        val tmp = ArgsMap("tmp")
        tmp["id"] = id
        tmp["msg"] = msg
        var run = true
        for (it in injects) {
            if (it.canMsg && !it.doBefore(tmp)) {
                run = false
                break
            }
        }
        val any = invoke(run, name, tmp)
        tmp.clear()
        return any
    }

    override suspend fun invoke(event: Event, p2: Event) {
        val tmp = ArgsMap("tmp") + event
        var run = true
        val list = ArrayDeque<Inject>()
        for (it in injects) {
            if (it.can(event)) {
                if (it.doBefore(tmp)) {
                    list.add(it)
                } else {
                    run = false
                    break
                }
            }
        }
        val any = invoke(run, name, tmp)
        while (list.size > 0) {
            list.removeLast().doAfter(event, tmp, any)
        }
        tmp.clear()
    }

    private suspend fun invoke(run: Boolean, name: String, tmp: ArgsMap): Any? {
        return if (run) try {
            logger.debug("$name 开始执行")
            invoke(tmp)
        } catch (e: Exception) {
            logger.warning(e)
            null
        } finally {
            logger.debug("$name 结束执行")
        }
        else null
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
        eventHandle: MiraiEventHandle,
        injector: InjectMap,
    ) : Caller(obj, property, eventHandle, injector) {
        private val callable = property.javaMethod!!

        init {
            callable.isAccessible = true
        }

        private val args = property.parameters.drop(1).map {
            it.type to it.annotations.qualifierName
        }

        override suspend operator fun invoke(tmp: ArgsMap): Any? {
            try {
                return callable.invoke(obj, *Array(args.size) {
                    val pair = args[it]
                    tmp[pair] ?: if (pair.first.isMarkedNullable) null else return null
                })
            } catch (e: Exception) {
                logger.error(e.cause ?: e)
                return null
            }
        }
    }

    class Func(
        obj: Any,
        private val property: KFunction<*>,
        eventHandle: MiraiEventHandle,
        injector: InjectMap,
    ) : Caller(obj, property, eventHandle, injector) {
        init {
            property.isAccessible = true
        }

        private val args = property.parameters.drop(1).map {
            it.type to it.annotations.qualifierName
        }

        override suspend operator fun invoke(tmp: ArgsMap): Any? {
            try {
                return property.callSuspend(obj, *Array(args.size) {
                    val pair = args[it]
                    tmp[pair] ?: if (pair.first.isMarkedNullable) null else return null
                })
            } catch (e: Exception) {
                logger.error(e.cause ?: e)
                return null
            }
        }
    }

    class JavaField(
        obj: Any,
        property: KProperty1<*, *>,
        eventHandle: MiraiEventHandle,
        injector: InjectMap,
    ) : Caller(obj, property, eventHandle, injector) {
        private val callable = property.javaField!!

        init {
            callable.isAccessible = true
        }

        override suspend operator fun invoke(tmp: ArgsMap): Any? = callable.get(obj)
    }

    class Property1(
        obj: Any,
        property: KProperty1<*, *>,
        eventHandle: MiraiEventHandle,
        injector: InjectMap,
    ) : Caller(obj, property, eventHandle, injector) {
        private val callable = property.getter

        init {
            callable.isAccessible = true
        }

        override suspend operator fun invoke(tmp: ArgsMap): Any? = callable.call(obj)
    }

    class Property2(
        obj: Any,
        property: KProperty2<*, *, *>,
        eventHandle: MiraiEventHandle,
        injector: InjectMap,
    ) : Caller(obj, property, eventHandle, injector) {
        private val callable = property.getter

        init {
            callable.isAccessible = true
        }

        private val arg = callable.parameters[1].run {
            type to annotations.qualifierName
        }

        override suspend operator fun invoke(tmp: ArgsMap): Any? {
            return callable.call(obj, tmp[arg] ?: if (arg.first.isMarkedNullable) null else return null)
        }
    }

    // endregion

    private inner class Inject(
        val ann: Annotation,
        val inj: Injector<Annotation, Event>,
    ) : Comparable<Inject> {
        override fun compareTo(other: Inject): Int = inj.weight.compareTo(other.inj.weight)
        fun init() = inj.doInit(ann, this@Caller)
        fun can(e: Event) = inj.event.isInstance(e)
        val canMsg = inj.canmsg
        suspend fun doBefore(tmp: ArgsMap) = inj.doBefore(ann, tmp, this@Caller)
        suspend fun doAfter(e: Event, tmp: ArgsMap, any: Any?) = inj.doAfter(ann, e, tmp, this@Caller, any)
        fun destory() = inj.doDestroy(ann, this@Caller)
    }
}
