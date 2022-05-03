package my.ktbot.utils.callor

import my.ktbot.annotation.AutoCall
import my.ktbot.interfaces.Plug
import java.lang.reflect.AnnotatedElement
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.javaField

object AutoCallor {

	fun add(vararg objs: Any): PriorityQueue<Plug> {
		val list = PriorityQueue<Plug>()
		for (obj in objs) {
			for (member: KCallable<*> in obj::class.declaredMembers) {
				val autoCall: AutoCall = member.AutoCall() ?: (member as? KProperty<*>)?.run {
					getter.AutoCall() ?: member.javaField?.AutoCall()
				} ?: continue
				val caller = Caller(obj, member, autoCall)
				list += caller
				ObjectMap.global[caller.name] = caller
			}
		}
		return list
	}

	private fun KCallable<*>.AutoCall() = annotations.filterIsInstance<AutoCall>().firstOrNull()

	private fun AnnotatedElement.AutoCall() = annotations.filterIsInstance<AutoCall>().firstOrNull()

}
