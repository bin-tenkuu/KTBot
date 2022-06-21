package my.miraiplus

import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class ObjectMap(name: String? = null) : Cloneable {
	private val list = ArrayList<SortObject<*>>()

	init {
		set(name, this)
	}

	// region getter, setter, remove

	operator fun <T : Any> get(name: String? = null, defaultValue: T) =
		this[defaultValue::class.javaObjectType, name] ?: defaultValue

	operator fun <T : Any> get(kClass: KClass<out T>, name: String? = null) = this[kClass.javaObjectType, name]
	operator fun <T : Any> get(clazz: Class<out T>, name: String? = null): T? {
		return list.filter {
			clazz.isInstance(it.obj) && (name == null || name == it.name)
		}.minOrNull()?.obj as T?
	}

	operator fun <T : Any> plusAssign(value: T) = this.add(value).run { }
	operator fun <T : Any> plus(value: T) = this.add(value)
	operator fun <T : Any> set(name: String?, value: T) = this.add(value, name)

	fun <T : Any> add(value: T, name: String? = null, weight: Int = 0): ObjectMap {
		list.add(SortObject(value, weight, name))
		return this
	}

	fun <T : Any> remove(value: T) {
		list.removeIf {
			it.obj == value
		}
	}
	// endregion

	fun clear() = list.clear()

	public override fun clone(): ObjectMap {
		return ObjectMap().also {
			it.list.addAll(list)
		}
	}

	override fun toString(): String {
		return "ObjectMap(mapSize=${list.size})"
	}

	private inner class SortObject<T : Any>(
		@JvmField
		val obj: T,
		private val weight: Int,
		val name: String?,
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
