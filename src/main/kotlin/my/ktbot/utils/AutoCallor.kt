package my.ktbot.utils

import my.ktbot.annotation.AutoCall
import net.mamoe.mirai.console.util.cast
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredMembers

class AutoCallor {
	private val list = PriorityQueue<Caller>()

	suspend fun forEach(map: ObjectMap) {
		for (any in list) any(map)
	}

	fun add(obj: Any) {
		for (member in obj::class.declaredMembers) {
			when (member) {
				is KFunction<*> -> {
					val autoCall = member.annotations.filterIsInstance<AutoCall>().firstOrNull()
					if (autoCall !== null) {
						list += Caller(obj, member, autoCall.weight)
					}
				}
				is KProperty<*> -> {
					val autoCall = member.getter.annotations.filterIsInstance<AutoCall>().firstOrNull()
					if (autoCall !== null) {
						list += Caller(obj, member, autoCall.weight)
					}
				}
			}
		}
	}

	class Caller constructor(
		private val obj: Any,
		private val callable: KCallable<*>,
		private val weight: Int
	) : Comparable<Caller> {
		private val args = callable.parameters.drop(1).map {
			it.type.classifier.cast<KClass<*>>().java
		}
		internal val set = args.toSet()

		suspend operator fun invoke(map: ObjectMap): Any? {
			return callable.callSuspend(obj, *Array(args.size) {
				map[args[it]] ?: return null
			})
		}

		override operator fun compareTo(other: Caller): Int {
			return weight.compareTo(other.weight)
		}
	}

	class ObjectMap : Cloneable {
		private val map = HashMap<Class<*>, ArrayList<*>>()
		operator fun <T : Any> get(kClass: KClass<T>): T? {
			return this[kClass.java]
		}

		operator fun <T : Any> get(clazz: Class<T>): T? {
			return get0(clazz).firstOrNull()
		}

		@Suppress("UNCHECKED_CAST") private fun <T : Any> get0(clazz: Class<T>): ArrayList<T> {
			return map.computeIfAbsent(clazz) { ArrayList<T>() } as ArrayList<T>
		}

		@Suppress("UNCHECKED_CAST") operator fun <T : Any> plusAssign(value: T) {
			this[value::class.java as Class<T>] = value
		}

		@Suppress("UNCHECKED_CAST") operator fun <T : Any> plus(value: T): ObjectMap {
			this[value::class.java as Class<T>] = value
			return this
		}

		operator fun <T : Any> set(kClass: KClass<T>, value: T): ObjectMap {
			this[kClass.java] = value
			return this
		}

		@Suppress("UNCHECKED_CAST") operator fun <T : Any> set(clazz: Class<T>, value: T): ObjectMap {
			get0(clazz).add(0, value)
			var tmp: Class<*>? = clazz.superclass
			while (tmp != null) {
				get0(tmp as Class<T>).add(value)
				for (iclazz: Class<*> in tmp.interfaces) {
					get0(iclazz as Class<T>).add(value)
				}
				tmp = tmp.superclass
			}
			return this
		}

		override fun clone(): ObjectMap {
			return this
		}
	}
}
