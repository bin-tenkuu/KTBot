package my.miraiplus

import kotlin.reflect.KClass

class ArgsMap(name: String? = null) {
	private val map = HashMap<Class<*>, HashMap<String?, Any>>(16)

	init {
		this[name] = this
	}

	// region getter, setter, remove, clear

	operator fun <T : Any> get(name: String? = null, defaultValue: T) =
		get(defaultValue.javaClass, name) ?: defaultValue

	operator fun <T : Any> get(kClass: KClass<out T>, name: String? = null) = get(kClass.javaObjectType, name)
	operator fun <T : Any> get(clazz: Class<out T>, name: String? = null): T? {
		@Suppress("UNCHECKED_CAST")
		return map(clazz) {
			get(name) ?: values.firstOrNull()
		} as T?
	}

	inline infix operator fun <reified T : Any> plus(value: T) = add(value)
	inline fun <reified T : Any> add(value: T) = set(null, value)
	operator fun <T : Any> set(name: String? = null, value: T): ArgsMap {
		map.computeIfAbsent(value.javaClass) { HashMap(4) }[name] = value
		return this
	}

	infix operator fun <T : Any> minus(kClass: KClass<T>) = remove(kClass)
	fun <T : Any> remove(kClass: KClass<T>) = remove(kClass.java)
	fun <T : Any> remove(clazz: Class<T>) {
		map(clazz) {
			clear()
			it.remove()
			null
		}
	}

	fun clear() = remove(Any::class.java)
	private operator fun <T : Any, V, R> HashMap<Class<*>, V>.invoke(clazz: Class<T>, block: V.(Remove) -> R?): R? {
		return this[clazz]?.block(Remove()) ?: run {
			val iterator = entries.iterator()
			val r = Remove(iterator)
			for (entry in iterator) {
				// [isAssignableFrom] 左边是父类，右边是子类
				if (clazz.isAssignableFrom(entry.key)) {
					return entry.value.block(r) ?: continue
				}
			}
			return null
		}
	}

	@JvmInline
	value class Remove(private val it: MutableIterator<*>? = null) {
		fun remove() = it?.remove()
	}
	// endregion

	override fun toString() = "ObjectMap(mapSize=${map.size})"

	companion object {
		val global = ArgsMap("global")
	}
}
