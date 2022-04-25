package my.ktbot.utils.callor

import java.util.*
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class ObjectMap : Cloneable {
	private val map = HashMap<Class<*>, PriorityQueue<SortObject<*>>>()
	operator fun <T : Any> get(kClass: KClass<T>): T? {
		return this[kClass.java]
	}

	operator fun <T : Any> get(clazz: Class<T>): T? {
		val sortObject = get0(clazz).peek() ?: return null
		return sortObject.obj as T?
	}

	private fun get0(clazz: Class<*>): PriorityQueue<SortObject<*>> {
		return map.computeIfAbsent(clazz) { PriorityQueue<SortObject<*>>(1) }
	}

	operator fun <T : Any> plusAssign(value: T) {
		this[value::class.java as Class<T>] = value
	}

	operator fun <T : Any> plus(value: T): ObjectMap {
		this[value::class.java as Class<T>] = value
		return this
	}

	operator fun <T : Any> set(kClass: KClass<T>, value: T): ObjectMap {
		this[kClass.java] = value
		return this
	}

	operator fun <T : Any> set(clazz: Class<T>?, value: T): ObjectMap {
		var tmp: Class<*>? = clazz
		var weight = 0
		while (tmp != null) {
			get0(tmp).add(SortObject(value, weight))
			for (iclazz: Class<*> in tmp.interfaces) {
				get0(iclazz).add(SortObject(value, weight))
			}
			tmp = tmp.superclass
			weight++
		}
		return this
	}

	override fun clone(): ObjectMap {
		return ObjectMap().also {
			it.map.putAll(map)
		}
	}

	private class SortObject<T>(
		@JvmField
		val obj: T, private val weight: Int
	) : Comparable<SortObject<*>> {
		override operator fun compareTo(other: SortObject<*>) = weight.compareTo(other.weight)
		override fun hashCode() = obj.hashCode()
		override fun toString() = obj.toString()
		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (other !is SortObject<*>) return false

			if (obj != other.obj) return false
			if (weight != other.weight) return false

			return true
		}
	}
}
