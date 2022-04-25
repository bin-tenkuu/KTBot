package my.ktbot.utils.callor

import my.ktbot.annotation.AutoCall
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMembers

class AutoCallor {
	private val list = PriorityQueue<Caller>()

	suspend fun forEach(map: ObjectMap) {
		for (any in list) any(map)
	}

	fun add(obj: Any) {
		for (member: KCallable<*> in obj::class.declaredMembers) {
			val autoCall: AutoCall = when (member) {
				is KFunction<*> -> member.annotations.filterIsInstance<AutoCall>().firstOrNull()
				is KProperty<*> -> member.getter.annotations.filterIsInstance<AutoCall>().firstOrNull()
				else -> null
			} ?: return
			list += Caller(obj, member, autoCall)
		}
	}
}
