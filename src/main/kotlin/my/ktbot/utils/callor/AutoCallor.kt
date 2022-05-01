package my.ktbot.utils.callor

import my.ktbot.annotation.AutoCall
import my.ktbot.interfaces.Plug
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMembers

object AutoCallor {

	fun add(vararg objs: Any): PriorityQueue<Plug> {
		val list = PriorityQueue<Plug>()
		for (obj in objs) {
			for (member: KCallable<*> in obj::class.declaredMembers) {
				val autoCall: AutoCall = when (member) {
					is KFunction<*> -> member.annotations.filterIsInstance<AutoCall>().firstOrNull()
					is KProperty<*> -> member.getter.annotations.filterIsInstance<AutoCall>().firstOrNull()
					else -> null
				} ?: continue
				val caller = Caller(obj, member, autoCall)
				list += caller
				ObjectMap.global[caller.name] = caller
			}
		}
		return list
	}
}
