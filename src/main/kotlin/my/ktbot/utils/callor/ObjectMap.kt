package my.ktbot.utils.callor

import java.util.*
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class ObjectMap(name: String? = null) : Cloneable {
	private val map = HashMap<Class<*>, PriorityQueue<SortObject<*>>>()

	init {
		set(name, this)
	}

	// region getter, setter

	operator fun <T : Any> get(kClass: KClass<out T>, name: String? = null) = this[kClass.java, name]
	operator fun <T : Any> get(clazz: Class<out T>, name: String? = null): T? {
		val queue = map[clazz] ?: return null
		if (name == null) return queue.firstOrNull()?.obj as T?
		return queue.firstOrNull { name == it.name }?.obj as T?
	}

	private fun get0(clazz: Class<*>): PriorityQueue<SortObject<*>> {
		return map.computeIfAbsent(clazz) { PriorityQueue<SortObject<*>>(1) }
	}

	operator fun <T : Any> plusAssign(value: T) = this.plus(value).run { }
	operator fun <T : Any> plus(value: T) = this.set(value::class.java as Class<T>, value)
	operator fun <T : Any> set(name: String?, value: T) = this.set(value::class.java as Class<T>, name, value)
	operator fun <T : Any> set(kClass: KClass<T>, value: T) = this.set(kClass.java, null, value)
	operator fun <T : Any> set(clazz: Class<T>, value: T) = this.set(clazz, null, value)
	operator fun <T : Any> set(kClass: KClass<T>, name: String?, value: T) = this.set(kClass.java, name, value)
	operator fun <T : Any> set(clazz: Class<T>, name: String?, value: T) = set(clazz, value, name, 0)
	fun <T : Any> set(clazz: Class<T>, value: T, name: String?, initWeight: Int = 0): ObjectMap {
		val set = HashSet<Class<*>>()
		var tmp: Class<*>? = clazz
		var weight = initWeight
		while (tmp != null) {
			get0(tmp).add(SortObject(value, weight, name))
			for (iclazz: Class<*> in tmp.interfaces) {
				if (set.add(iclazz)) {
					get0(iclazz).add(SortObject(value, weight, name))
				}
			}
			tmp = tmp.superclass
			weight++
		}
		return this
	}

	// endregion

	fun clear() = map.clear()

	public override fun clone(): ObjectMap {
		return ObjectMap().also {
			for ((clazz, list) in map) {
				it.get0(clazz).addAll(list)
			}
		}
	}

	override fun toString(): String {
		return "ObjectMap(mapSize=${map.size})"
	}

	private inner class SortObject<T>(
		@JvmField
		val obj: T,
		private val weight: Int,
		val name: String?
	) : Comparable<SortObject<*>> {
		override operator fun compareTo(other: SortObject<*>) = weight.compareTo(other.weight)
		override fun hashCode() = obj.hashCode()
		override fun toString() = obj.toString()
		override fun equals(other: Any?) = when {
			this === other -> true
			other !is SortObject<*> -> false
			obj != other.obj -> false
			else -> true
		}
	}

	companion object {
		val global = ObjectMap("global")
	}
}
