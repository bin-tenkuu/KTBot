package my.miraiplus.injector

import kotlin.reflect.KClass

class InjectMap {
	private val injectorMap = HashMap<Class<out Annotation>, ArrayList<Injector<out Annotation>>>()

	// region add

	fun <T : Annotation> add(annClass: Class<T>, injector: Injector<T>): Boolean {
		return injectorMap.getOrPut(annClass) { ArrayList() }.add(injector)
	}

	fun <T : Annotation> add(annClass: KClass<T>, injector: Injector<T>) = add(annClass.java, injector)

	inline fun <reified T : Annotation> add(injector: Injector<T>) = add(T::class.java, injector)

	inline operator fun <reified T : Annotation> plus(injector: Injector<T>): InjectMap {
		add(T::class.java, injector)
		return this
	}

	inline operator fun <reified T : Annotation> plusAssign(injector: Injector<T>) {
		add(T::class.java, injector)
	}

	// endregion

	// region remove

	fun <T : Annotation> remove(annClass: Class<T>, injector: Injector<T>): Boolean {
		return injectorMap[annClass]?.remove(injector) ?: false
	}

	fun <T : Annotation> remove(annClass: KClass<T>, injector: Injector<T>) = remove(annClass.java, injector)

	inline fun <reified T : Annotation> remove(injector: Injector<T>) = remove(T::class.java, injector)

	inline operator fun <reified T : Annotation> minus(injector: Injector<T>): InjectMap {
		remove(T::class.java, injector)
		return this
	}

	inline operator fun <reified T : Annotation> minusAssign(injector: Injector<T>) {
		remove(T::class.java, injector)
	}

	// endregion

	// region get

	@Suppress("UNCHECKED_CAST")
	operator fun <T : Annotation> get(annClass: Class<T>): MutableList<Injector<T>>? {
		return injectorMap[annClass] as MutableList<Injector<T>>?
	}

	@Suppress("UNCHECKED_CAST")
	operator fun <T : Annotation> get(annClass: KClass<T>) = this[annClass.java]

	inline fun <reified T : Annotation> get() = get(T::class.java)

	// endregion
}
