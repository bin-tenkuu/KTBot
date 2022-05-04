package my.ktbot.utils.callor

import my.ktbot.annotation.AutoCall
import my.ktbot.interfaces.Plug
import java.lang.reflect.AnnotatedElement
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.javaField

object AutoCallor {

	fun add(vararg objs: Any): PriorityQueue<Plug> {
		val list = PriorityQueue<Plug>()
		for (obj: Any in objs) {
			for (member: KCallable<*> in obj::class.declaredMembers) {
				val caller: Caller
				when (member) {
					is KFunction<*> -> {
						val autoCall = member.AutoCall() ?: continue
						caller = Caller.Func(obj, member, autoCall)
					}
					is KProperty1<*, *> -> {
						val field = member.javaField
						val autoCall = member.AutoCall() ?: member.getter.AutoCall() ?: field?.AutoCall() ?: continue
						caller = (if (field !== null) Caller.JavaField(obj, field, autoCall)
						else Caller.Property1(obj, member, autoCall))
					}
					is KProperty2<*, *, *> -> {
						val autoCall = member.AutoCall() ?: member.getter.AutoCall() ?: continue
						caller = Caller.Property2(obj, member, autoCall)
					}
					else -> {
						System.err.println(member)
						continue
					}
				}
				list += caller
				ObjectMap.global[caller.name] = caller
			}
		}
		return list
	}

	private fun KCallable<*>.AutoCall() = annotations.filterIsInstance<AutoCall>().firstOrNull()

	private fun AnnotatedElement.AutoCall() = annotations.filterIsInstance<AutoCall>().firstOrNull()

}
